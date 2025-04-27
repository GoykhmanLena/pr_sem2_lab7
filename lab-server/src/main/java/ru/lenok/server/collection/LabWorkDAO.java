package ru.lenok.server.collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.common.models.Difficulty;
import ru.lenok.common.models.LabWork;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

public class LabWorkDAO {
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
                discipline_practice_hours
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::difficulty, ?, ?)
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

    public LabWorkDAO(Hashtable<String, LabWork> initialState, String dbHost, String dbPort, String dbUser, String dbPassword) throws SQLException {
        init(initialState, dbHost, dbPort, dbUser, dbPassword);
    }

    private void init(Hashtable<String, LabWork> initialState, String dbHost, String dbPort, String dbUser, String dbPassword) throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%s/studs?currentSchema=s465561", dbHost, dbPort);

        connection = DriverManager.getConnection(url, dbUser, dbPassword);
        logger.info("Подключение к PostgreSQL успешно!");

        initScheme(true);
        persistInitialState(initialState);
    }

    private void persistInitialState(Hashtable<String, LabWork> initialState) throws SQLException {
        for (String key : initialState.keySet()) {
            LabWork labWork = initialState.get(key);
            insert(key, labWork);
        }
    }


    public void close() throws SQLException {
        connection.close();
    } //TODO

    private void initScheme(boolean reInitDb) throws SQLException {
        String dropALL =
                "DROP INDEX IF EXISTS idx_labwork_name;\n" +
                        "DROP INDEX IF EXISTS idx_labwork_unique_key;\n" +
                        "DROP TABLE IF EXISTS lab_work;\n" +
                        "DROP TYPE IF EXISTS DIFFICULTY;";

        String createSequence = "CREATE SEQUENCE IF NOT EXISTS lab_work_id START 1;";
        String list = Arrays.stream(Difficulty.values())
                .map(d -> "'" + d.name() + "'")
                .collect(Collectors.joining(", "));

        String createType =
                "DO $$\n" +
                        "    BEGIN\n" +
                        "        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'difficulty') THEN\n" +
                        "            CREATE TYPE DIFFICULTY AS ENUM (" + list + ");\n" +
                        "        END IF;\n" +
                        " END $$;";

        String createTable = "CREATE TABLE IF NOT EXISTS lab_work (\n" +
                "                       id BIGINT DEFAULT nextval('lab_work_id') PRIMARY KEY,\n" +
                "                       key VARCHAR(256) NOT NULL,\n" +
                "                       name VARCHAR(256) NOT NULL,\n" +
                "                       coord_x DOUBLE PRECISION NOT NULL,\n" +
                "                       coord_y REAL NOT NULL,\n" +
                "                       creation_date TIMESTAMP NOT NULL,\n" +
                "                       minimal_point DOUBLE PRECISION NOT NULL,\n" +
                "                       description VARCHAR(2863) NOT NULL,\n" +
                "                       difficulty DIFFICULTY NOT NULL,\n" +
                "                       discipline_name VARCHAR(256),\n" +
                "                       discipline_practice_hours BIGINT NOT NULL\n" +
                ");";
        String createIndexName = "CREATE INDEX IF NOT EXISTS idx_labwork_name ON lab_work (name);";
        String createIndexKey = "CREATE UNIQUE INDEX IF NOT EXISTS idx_labwork_unique_key ON lab_work (key);";
        try (Statement stmt = connection.createStatement()) {
            if (reInitDb){
                stmt.executeUpdate(dropALL);
            }
            stmt.executeUpdate(createSequence);
            stmt.executeUpdate(createType);
            stmt.executeUpdate(createTable);
            stmt.executeUpdate(createIndexName);
            stmt.executeUpdate(createIndexKey);
        }
    }
    public void insert(String key, LabWork labWork) throws SQLException{
        try (PreparedStatement pstmt = connection.prepareStatement(CREATE_LAB_WORK)) {

            // Устанавливаем параметры
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

            // Выполняем вставку
            int rowsInserted = pstmt.executeUpdate();
            logger.info("Вставлено строк: " + rowsInserted);
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
                        .build();

                result.put(rs.getString("key"), labWork);
            }
        }
        return result;
    }

    public int countAll() throws SQLException{
        int result = 0;
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_COUNT_ALL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                result = rs.getInt("count");
            }
        }
        return result;
    }

}