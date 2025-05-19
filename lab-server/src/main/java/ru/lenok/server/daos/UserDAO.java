package ru.lenok.server.daos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.common.auth.User;
import ru.lenok.server.utils.PasswordHasher;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static ru.lenok.server.daos.SQLQueries.*;

public class UserDAO extends AbstractDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    public UserDAO(Set<Long> initialState, DBConnector dbConnector, boolean reinitDB) throws SQLException, NoSuchAlgorithmException {
        super(dbConnector.getDatasource());
        init(initialState, reinitDB);
    }

    private void init(Set<Long> initialState, boolean reinitDB) throws SQLException, NoSuchAlgorithmException {
        initScheme(reinitDB);
        if (reinitDB) {
            persistInitialState(initialState);
        }
    }

    private void initScheme(boolean reinitDB) throws SQLException {
        try (Connection connection = ds.getConnection(); Statement stmt = connection.createStatement()) {
            if (reinitDB) {
                stmt.executeUpdate(DROP_ALL_OFFER.t());
                stmt.executeUpdate(DROP_ALL_LABWORK.t());
                stmt.executeUpdate(DROP_ALL_USERS.t());
            }
            stmt.executeUpdate(CREATE_SEQUENCE_USER.t());
            stmt.executeUpdate(CREATE_TABLE_USER.t());
            stmt.executeUpdate(CREATE_INDEX_USER.t());
        }
        printSequence("user_seq");
    }

    private void persistInitialState(Set<Long> initialState) throws SQLException, NoSuchAlgorithmException {
        long maxId = 1L;
        Map<Long, String> people = new HashMap<>();
        people.put(1L, "Gavrilov");
        people.put(2L, "Klimenkov");
        people.put(3L, "Balakshin");
        people.put(4L, "Holodova");
        people.put(5L, "User5");
        for (Long userId : initialState) {
            User user = new User(userId, people.get(userId), "1");
            insert(user);
            maxId = Math.max(maxId, userId);
        }
        setSequenceValue("user_seq", maxId);
        printSequence("user_seq");
    }

    public User insert(User user) throws SQLException, NoSuchAlgorithmException {
        String sql = user.getId() != null ? CREATE_USER_WITH_ID.t() : CREATE_USER.t();
        try (Connection connection = ds.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            String userPassword = user.getPassword();
            pstmt.setString(2, PasswordHasher.hash(userPassword));

            if (user.getId() != null) {
                pstmt.setLong(3, user.getId());
            }

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    long userId = resultSet.getLong(1);
                    User userFromDb = new User(userId, user.getUsername(), PasswordHasher.hash(userPassword));
                    return userFromDb;
                } else {
                    throw new SQLException("Ошибка при вставке пользователя, " + user);
                }
            }
        }
    }

    public User getUserByName(String name) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(GET_USER_BY_NAME.t())) {
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
    }
}
