package ru.lenok.server.daos;

import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.common.models.Difficulty;
import ru.lenok.common.models.LabWork;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class LabWorkDAO {
    private static final String DELETE_FOR_USER = "DELETE FROM lab_work WHERE owner_id = ?";
    private static final String DELETE_BY_KEYS = "DELETE FROM lab_work WHERE key = ANY (?)";
    private static final String DELETE_LAB_WORK = "DELETE FROM lab_work WHERE key = ?";
    private static final String TRUNCATE_LABS = "TRUNCATE TABLE lab_work";

    private static final String UPDATE_LAB_WORK = """
            UPDATE lab_work
            SET
                key = ?,
                name = ?,
                coord_x = ?,
                coord_y = ?,
                creation_date = ?,
                minimal_point = ?,
                description = ?,
                difficulty = ?,
                discipline_name = ?,
                discipline_practice_hours = ?,
                owner_id = ?
            WHERE id = ?
            """;
    private static final String CREATE_LAB_WORK = """
                INSERT INTO lab_work (
                    key,
                    name,
                    coord_x,
                    coord_y,
                    creation_date,
                    minimal_point,
                    description,
                    difficulty,
                    discipline_name,
                    discipline_practice_hours,
                    owner_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::difficulty, ?, ?, ?)
            """;

    private static final String CREATE_LAB_WORK_WITH_ID = """
                INSERT INTO lab_work (
                    key,
                    name,
                    coord_x,
                    coord_y,
                    creation_date,
                    minimal_point,
                    description,
                    difficulty,
                    discipline_name,
                    discipline_practice_hours,
                    owner_id,
                    id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::difficulty, ?, ?, ?, ?)
            """;

    private static final String SELECT_ALL = """
            SELECT *
            FROM lab_work
            ORDER BY name
            """;

    private static final String SELECT_COUNT_ALL = """
            SELECT COUNT(id) AS count
            FROM lab_work
            """;
    private static final Logger logger = LoggerFactory.getLogger(LabWorkDAO.class);
    private Connection connection;

    public LabWorkDAO(Hashtable<String, LabWork> initialState, DBConnector dbConnector, boolean dbReinit, String dbSchema) throws SQLException {
        connection = dbConnector.getConnection();
        init(initialState, dbReinit, dbSchema);
    }

    private void init(Hashtable<String, LabWork> initialState, boolean dbReinit, String dbSchema) throws SQLException {
        initScheme(dbReinit, dbSchema);
        if (!initialState.isEmpty()) {
            persistInitialState(initialState);
        }
    }

    private void persistInitialState(Hashtable<String, LabWork> initialState) throws SQLException {
        Long maxId = 1L;
        for (String key : initialState.keySet()) {
            LabWork labWork = initialState.get(key);
            insert(key, labWork);
            maxId = Math.max(labWork.getId(), maxId);
        }

        setSequenceValue(maxId);
    }


    public void close() throws SQLException {
        connection.close();
    } //TODO

    private void initScheme(boolean reinitDB, String dbSchema) throws SQLException {
            String dropALL =
                    "DROP INDEX IF EXISTS idx_labwork_name;\n" +
                            "DROP INDEX IF EXISTS idx_labwork_unique_key;\n" +
                            "DROP TABLE IF EXISTS lab_work;\n" +
                            "DROP SEQUENCE IF EXISTS lab_work_seq;" +
                            "DROP TYPE IF EXISTS DIFFICULTY;";

        String createSequence = "CREATE SEQUENCE IF NOT EXISTS lab_work_seq START 1;";
        String list = Arrays.stream(Difficulty.values())
                .map(d -> "'" + d.name() + "'")
                .collect(Collectors.joining(", "));

        String createType =
                "DO $$\n" +
                        "DECLARE\n" +
                        "    schema_oid oid;\n" +
                        "BEGIN\n" +
                        "    SELECT oid INTO schema_oid FROM pg_namespace WHERE nspname = '" + dbSchema + "';\n" +
                        "    IF NOT EXISTS (\n" +
                        "        SELECT 1 FROM pg_type t WHERE t.typname = 'difficulty' AND t.typnamespace = schema_oid\n" +
                        "    ) THEN\n" +
                        "        CREATE TYPE difficulty AS ENUM (" + list + ");\n" +
                        "    END IF;\n" +
                "END $$;";


        String createTable = "CREATE TABLE IF NOT EXISTS lab_work (\n" +
                "                       id BIGINT DEFAULT nextval('lab_work_seq') PRIMARY KEY,\n" +
                "                       key VARCHAR(256) NOT NULL,\n" +
                "                       name VARCHAR(256) NOT NULL,\n" +
                "                       coord_x DOUBLE PRECISION NOT NULL,\n" +
                "                       coord_y REAL NOT NULL,\n" +
                "                       creation_date TIMESTAMP NOT NULL,\n" +
                "                       minimal_point DOUBLE PRECISION NOT NULL,\n" +
                "                       description VARCHAR(2863) NOT NULL,\n" +
                "                       difficulty DIFFICULTY NOT NULL,\n" +
                "                       discipline_name VARCHAR(256),\n" +
                "                       discipline_practice_hours BIGINT NOT NULL,\n" +
                "                       owner_id BIGINT REFERENCES users(id)" +
                ");";
        String createIndexName = "CREATE INDEX IF NOT EXISTS idx_labwork_name ON lab_work (name);";
        String createIndexKey = "CREATE UNIQUE INDEX IF NOT EXISTS idx_labwork_unique_key ON lab_work (key);";
        try (Statement stmt = connection.createStatement()) {
            if (reinitDB) {
                stmt.executeUpdate(dropALL);
            }
            stmt.executeUpdate(createSequence);
            stmt.executeUpdate(createType);
            stmt.executeUpdate(createTable);
            stmt.executeUpdate(createIndexName);
            stmt.executeUpdate(createIndexKey);
        }
    }

    public Long insert(String key, LabWork labWork) throws SQLException {
        String sql = labWork.getId() != null ? CREATE_LAB_WORK_WITH_ID : CREATE_LAB_WORK;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, key);
            pstmt.setString(2, labWork.getName());
            pstmt.setDouble(3, labWork.getCoordinates().getX());
            pstmt.setFloat(4, labWork.getCoordinates().getY());
            pstmt.setTimestamp(5, Timestamp.valueOf(labWork.getCreationDate()));
            pstmt.setDouble(6, labWork.getMinimalPoint());
            pstmt.setString(7, labWork.getDescription());
            pstmt.setString(8, labWork.getDifficulty().name());
            pstmt.setString(9, labWork.getDiscipline().getName());
            pstmt.setLong(10, labWork.getDiscipline().getPracticeHours());
            pstmt.setLong(11, labWork.getOwnerId());

            if (labWork.getId() != null) {
                pstmt.setLong(12, labWork.getId());
            }

            int rowsInserted = pstmt.executeUpdate();
            logger.info("Вставлено строк: " + rowsInserted);
            return getSequenceValue();
        }
    }

    public void updateById(String key, LabWork labWork) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_LAB_WORK)) {
            PGobject difficulty = new PGobject();
            difficulty.setType("difficulty");
            difficulty.setValue(labWork.getDifficulty().name());


            pstmt.setString(1, key);
            pstmt.setString(2, labWork.getName());
            pstmt.setDouble(3, labWork.getCoordinates().getX());
            pstmt.setFloat(4, labWork.getCoordinates().getY());
            pstmt.setTimestamp(5, Timestamp.valueOf(labWork.getCreationDate()));
            pstmt.setDouble(6, labWork.getMinimalPoint());
            pstmt.setString(7, labWork.getDescription());
            pstmt.setObject(8, difficulty);
            pstmt.setString(9, labWork.getDiscipline().getName());
            pstmt.setLong(10, labWork.getDiscipline().getPracticeHours());
            pstmt.setLong(11, labWork.getOwnerId());
            pstmt.setLong(12, labWork.getId());

            pstmt.executeUpdate();
        }
    }

    public void delete(String key) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_LAB_WORK)) {
            pstmt.setString(1, key);
            pstmt.executeUpdate();
        }
    }

    public void deleteAll() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(TRUNCATE_LABS);
        }
    }

    public void deleteForUser(long ownerId) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_FOR_USER)) {
            pstmt.setLong(1, ownerId);
            pstmt.executeUpdate();
        }
    }



        public Map<String, LabWork> selectAll() throws SQLException {
        HashMap<String, LabWork> result = new HashMap<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                LabWork.Builder builder = new LabWork.Builder();
                LabWork labWork = builder
                        .setId(rs.getLong("id"))
                        .setName(rs.getString("name"))
                        .setCoordinateX(rs.getDouble("coord_x"))
                        .setCoordinateY(rs.getFloat("coord_y"))
                        .setCreationDate(rs.getTimestamp("creation_date"))
                        .setMinimalPoint(rs.getDouble("minimal_point"))
                        .setDescription(rs.getString("description"))
                        .setDifficulty(Difficulty.valueOf(rs.getString("difficulty")))
                        .setDisciplineName(rs.getString("discipline_name"))
                        .setDisciplinePracticeHours(rs.getLong("discipline_practice_hours"))
                        .setOwnerId(rs.getLong("owner_id"))
                        .build();

                result.put(rs.getString("key"), labWork);
            }
        }
        return result;
    }

    public int countAll() throws SQLException {
        int result = 0;
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_COUNT_ALL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                result = rs.getInt("count");
            }
        }
        return result;
    }

    public void deleteByKeys(List<String> keysForRemoving) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_BY_KEYS)) {
            Array keyArray = connection.createArrayOf("varchar", keysForRemoving.toArray());
            pstmt.setArray(1, keyArray);

            pstmt.executeUpdate();
        }
    }

    public void setSequenceValue(long newValue) throws SQLException {
        String sql = "SELECT setval('lab_work_seq', ?, true)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, newValue);
            statement.executeQuery();
        }
    }

    private Long getSequenceValue(){
        String query = "SELECT last_value FROM " + "lab_work_seq";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                long lastValue = rs.getLong("last_value");
                return lastValue;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}