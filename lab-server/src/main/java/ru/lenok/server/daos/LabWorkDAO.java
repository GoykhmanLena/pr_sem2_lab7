package ru.lenok.server.daos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.common.models.Difficulty;
import ru.lenok.common.models.LabWork;

import java.sql.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static ru.lenok.server.daos.SQLQueries.*;

public class LabWorkDAO extends AbstractDAO {
    private static final Logger logger = LoggerFactory.getLogger(LabWorkDAO.class);

    public LabWorkDAO(Hashtable<String, LabWork> initialState, DBConnector dbConnector, boolean dbReinit) throws SQLException {
        super(dbConnector.getDatasource());
        init(initialState, dbReinit);
    }

    private void init(Hashtable<String, LabWork> initialState, boolean dbReinit) throws SQLException {
        initScheme(dbReinit);
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
        setSequenceValue("lab_work_seq", maxId);
    }

    private void initScheme(boolean reinitDB) throws SQLException {
        try (Connection connection = ds.getConnection(); Statement stmt = connection.createStatement()) {
            if (reinitDB) {
                stmt.executeUpdate(DROP_ALL_LABWORK.t());
            }
            stmt.executeUpdate(CREATE_SEQUENCE_LABWORK.t());
            stmt.executeUpdate(CREATE_TABLE_LABWORK.t());
            stmt.executeUpdate(CREATE_NAME_INDEX_LABWORK.t());
            stmt.executeUpdate(CREATE_KEY_INDEX_LABWORK.t());
        }
    }

    public Long insert(String key, LabWork labWork) throws SQLException {
        String sql = labWork.getId() != null ? CREATE_LAB_WORK_WITH_ID.t() : CREATE_LAB_WORK.t();
        try (Connection connection = ds.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
            long id;

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    id = resultSet.getLong(1);
                } else {
                    throw new SQLException("Ошибка при вставке LabWork");
                }
            }
            return id;
        }
    }

    public void updateById(String key, LabWork labWork, Connection connection) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_LAB_WORK.t())) {
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
            pstmt.setLong(12, labWork.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(String key) throws SQLException {
        try (Connection connection = ds.getConnection(); PreparedStatement pstmt = connection.prepareStatement(DELETE_LABWORK.t())) {
            pstmt.setString(1, key);
            pstmt.executeUpdate();
        }
    }

    public void deleteForUser(long ownerId) throws SQLException {
        try (Connection connection = ds.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(DELETE_FOR_USER_LABWORK.t())) {
            pstmt.setLong(1, ownerId);
            pstmt.executeUpdate();
        }
    }


    public Map<String, LabWork> selectAll() throws SQLException {
        HashMap<String, LabWork> result = new HashMap<>();
        try (Connection connection = ds.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SELECT_ALL.t());
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

    public void deleteByKeys(List<String> keysForRemoving) throws SQLException {
        try (Connection connection = ds.getConnection(); PreparedStatement pstmt = connection.prepareStatement(DELETE_BY_KEYS_LABWORK.t())) {
            Array keyArray = connection.createArrayOf("varchar", keysForRemoving.toArray());
            pstmt.setArray(1, keyArray);

            pstmt.executeUpdate();
        }
    }
}