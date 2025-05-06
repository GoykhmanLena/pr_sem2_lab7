package ru.lenok.server.daos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.common.auth.User;
import ru.lenok.server.utils.PasswordHasher;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Set;

public class UserDAO {
    private final Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private static final String CREATE_USER = """
                INSERT INTO users (
                    name,
                    pw_hash
                ) VALUES (?, ?)
                RETURNING id
            """;

    private static final String CREATE_USER_WITH_ID = """
                INSERT INTO users (
                    name,
                    pw_hash,
                    id
                ) VALUES (?, ?, ?)
                RETURNING id
            """;

    public UserDAO(Set<Long> initialState, DBConnector dbConnector, boolean reinitDB) throws SQLException, NoSuchAlgorithmException {
        this.connection = dbConnector.getConnection();
        init(initialState, reinitDB);
    }

    private void init(Set<Long> initialState, boolean reinitDB) throws SQLException, NoSuchAlgorithmException {
        initScheme(reinitDB);
        if (reinitDB) {
            persistInitialState(initialState);
        }
    }

    private void initScheme(boolean reinitDB) throws SQLException {
        String dropAllLabWork =
                "DROP INDEX IF EXISTS idx_labwork_name;\n" +
                        "DROP INDEX IF EXISTS idx_labwork_unique_key;\n" +
                        "DROP TABLE IF EXISTS lab_work;\n" +
                        "DROP SEQUENCE IF EXISTS lab_work_seq;" +
                        "DROP TYPE IF EXISTS DIFFICULTY;";
        String dropALL =
                "DROP INDEX IF EXISTS idx_user_name;\n" +
                        "DROP TABLE IF EXISTS users;\n" +
                        "DROP SEQUENCE IF EXISTS user_seq;";

        String createSequence = "CREATE SEQUENCE IF NOT EXISTS user_seq START 1;";

        String createTable = "CREATE TABLE IF NOT EXISTS users (\n" +
                "                       id BIGINT DEFAULT nextval('user_seq') PRIMARY KEY,\n" +
                "                       name VARCHAR(256) NOT NULL UNIQUE,\n" +
                "                       pw_hash VARCHAR(256) NOT NULL\n" +
                ");";
        String createIndexName = "CREATE INDEX IF NOT EXISTS idx_user_name ON users (name);";

        try (Statement stmt = connection.createStatement()) {
            if (reinitDB) {
                stmt.executeUpdate(dropAllLabWork);
                stmt.executeUpdate(dropALL);
            }
            stmt.executeUpdate(createSequence);
            stmt.executeUpdate(createTable);
            stmt.executeUpdate(createIndexName);
        }
        printSequence();
    }

    private void persistInitialState(Set<Long> initialState) throws SQLException, NoSuchAlgorithmException {
        long maxId = 1L;
        for (Long userId : initialState) {
            User user = new User(userId, "user" + userId, "user" + userId);
            insert(user);
            maxId = Math.max(maxId, userId);
        }
        setSequenceValue(maxId);
        printSequence();
    }

    public User insert(User user) throws SQLException, NoSuchAlgorithmException {
        String sql = user.getId() != null ? CREATE_USER_WITH_ID : CREATE_USER;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            String UserPassword = user.getPassword();
            pstmt.setString(2, PasswordHasher.hash(UserPassword));

            if (user.getId() != null) {
                pstmt.setLong(3, user.getId());
            }

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    long userId = resultSet.getLong(1);
                    User userFromDb = new User(userId, user.getUsername(), PasswordHasher.hash(UserPassword));
                    return userFromDb;
                } else {
                    throw new SQLException("Ошибка при вставке пользователя, " + user);
                }
            }
        }
    }

    public User getUserByName(String name) throws SQLException {
        String query = "SELECT id, name, pw_hash FROM users WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    long id = resultSet.getLong("id");
                    String userName = resultSet.getString("name");
                    String pwHash = resultSet.getString("pw_hash");
                    return new User(id, userName, pwHash);
                } else {
                    return null;
                }
            }
        }
    }

    public void setSequenceValue(long newValue) throws SQLException {
        String sql = "SELECT setval('user_seq', ?, false)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, newValue);
            statement.executeQuery();
        }
    }

    private void printSequence(){
        String query = "SELECT last_value FROM " + "user_seq";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                long lastValue = rs.getLong("last_value");
                System.out.println("Current sequence value: " + lastValue);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
