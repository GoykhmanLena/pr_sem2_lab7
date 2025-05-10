package ru.lenok.server.daos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    private static final Logger logger = LoggerFactory.getLogger(DBConnector.class);
    private Connection connection;

    public DBConnector(String dbHost, String dbPort, String dbUser, String dbPassword, String dbSchema) throws SQLException {
        init(dbHost, dbPort, dbUser, dbPassword, dbSchema);
    }

    private void init(String dbHost, String dbPort, String dbUser, String dbPassword, String dbSchema) throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%s/studs?currentSchema=%s", dbHost, dbPort, dbSchema);

        connection = DriverManager.getConnection(url, dbUser, dbPassword);
        connection.setAutoCommit(false);
        logger.info("Подключение к PostgreSQL успешно!");
    }

    public Connection getConnection() {
        return connection;
    }
}