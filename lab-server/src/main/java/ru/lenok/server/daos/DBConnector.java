package ru.lenok.server.daos;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConnector {
    private static final Logger logger = LoggerFactory.getLogger(DBConnector.class);
    private DataSource ds;

    public DBConnector(String dbHost, String dbPort, String dbUser, String dbPassword, String dbSchema) throws SQLException {
        init(dbHost, dbPort, dbUser, dbPassword, dbSchema);
    }

    private void init(String dbHost, String dbPort, String dbUser, String dbPassword, String dbSchema) throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%s/studs?currentSchema=%s", dbHost, dbPort, dbSchema);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(dbUser);
        config.setPassword(dbPassword);
        config.setAutoCommit(true);
        config.setConnectionTimeout(5000);

        ds = new HikariDataSource(config);

        //connection = DriverManager.getConnection(url, dbUser, dbPassword);
        //connection.setAutoCommit(false);
        Connection connection = ds.getConnection();
        if (!connection.isValid(1000)) {
            throw new SQLException("Ошибка при подключении к БД:" + url);
        }
        logger.info("Подключение к PostgreSQL успешно!");
    }

    public DataSource getDatasource() throws SQLException {
        return ds;
    }
}