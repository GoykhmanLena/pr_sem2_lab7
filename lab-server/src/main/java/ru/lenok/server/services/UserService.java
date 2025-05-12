package ru.lenok.server.services;

import ru.lenok.common.auth.User;
import ru.lenok.server.daos.DBConnector;
import ru.lenok.server.daos.UserDAO;
import ru.lenok.server.utils.PasswordHasher;

import javax.sql.DataSource;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class UserService {
    private final UserDAO userDAO;
    private final DataSource ds;

    public UserService(UserDAO userDAO, DBConnector dbConnector) throws SQLException {
        ds = dbConnector.getDatasource();
        this.userDAO = userDAO;
    }

    public User register(User user) throws SQLException, NoSuchAlgorithmException {
        User userFromDB = userDAO.insert(user);
        return userFromDB;
    }

    public User login(User user) throws SQLException, IllegalArgumentException, NoSuchAlgorithmException {
        User userFromDb = userDAO.getUserByName(user.getUsername());
        if (userFromDb != null) {
            checkPassword(user, userFromDb);
            return userFromDb;
        }
        throw new IllegalArgumentException("Юзера с таким логином и паролем не существует");
    }

    private void checkPassword(User userFromRequest, User userFromDb) throws NoSuchAlgorithmException {
        String userPassword = userFromRequest.getPassword();
        if (!PasswordHasher.hash(userPassword).equals(userFromDb.getPassword())) {
            throw new IllegalArgumentException("Юзера с таким логином и паролем не существует");
        }
    }
}
