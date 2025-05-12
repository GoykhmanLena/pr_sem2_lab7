package ru.lenok.server.daos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;

public class AbstractDAO {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDAO.class);

    protected final DataSource ds;
    public AbstractDAO(DataSource ds) {
        this.ds =ds;
    }

    protected void setSequenceValue(String sequenceName, long newValue) throws SQLException {
        String sql = "SELECT setval('" + sequenceName + "', ?, false)";
        try (Connection connection = ds.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, newValue);
                statement.executeQuery();
            }
        }
    }

    protected void printSequence(String sequenceName) throws SQLException {
        String query = "SELECT last_value FROM " + sequenceName;
        try (Connection connection = ds.getConnection()) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    long lastValue = rs.getLong("last_value");
                    logger.debug(sequenceName + " value: " + lastValue);
                }
            }
        }
    }
}
