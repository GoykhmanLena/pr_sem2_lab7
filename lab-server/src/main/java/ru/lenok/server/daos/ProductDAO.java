package ru.lenok.server.daos;

import ru.lenok.common.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProductDAO {
    private Connection connection;
    private static final String CREATE_PRODUCT = """
                INSERT INTO product (
                    name,
                    owner_id
                ) VALUES (?, ?)
                RETURNING id
            """;
    private static final String SELECT_PRODUCTS_BY_OWNER = """
            SELECT * FROM product WHERE owner_id = ?
            """;
    private static final String SELECT_PRODUCTS_BY_ID = """
            SELECT * FROM product WHERE id = ?
            """;
    private static final String UPDATE_PRODUCT = """
            UPDATE product
            SET owner_id = ?,
            name = ?
            WHERE id = ?
            """;

    public ProductDAO(Set<Long> userIds, DBConnector dbConnector, boolean dbReinit) throws SQLException {
        connection = dbConnector.getConnection();
        init(userIds, dbReinit);
    }

    private void init(Set<Long> userIds, boolean dbReinit) throws SQLException {
        initScheme(dbReinit);
        if (dbReinit) {
            persistInitialState(userIds);
        }
    }

    private void initScheme(boolean reinitDB) throws SQLException {

        String dropALL =
                "DROP INDEX IF EXISTS idx_product_name;\n" +
                        "DROP TABLE IF EXISTS product;\n" +
                        "DROP SEQUENCE IF EXISTS product_seq;";

        String createSequence = "CREATE SEQUENCE IF NOT EXISTS product_seq START 1;";

        String createTable = "CREATE TABLE IF NOT EXISTS product (\n" +
                "                       id BIGINT DEFAULT nextval('product_seq') PRIMARY KEY,\n" +
                "                       name VARCHAR(256) NOT NULL,\n" +
                "                       owner_id BIGINT NOT NULL\n" +
                ");";
        String createIndexName = "CREATE INDEX IF NOT EXISTS idx_product_name ON product (name);";

        try (Statement stmt = connection.createStatement()) {
            if (reinitDB) {
                stmt.executeUpdate(dropALL);
            }
            stmt.executeUpdate(createSequence);
            stmt.executeUpdate(createTable);
            stmt.executeUpdate(createIndexName);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    private void persistInitialState(Set<Long> userIds) throws SQLException {
        try {
            for (Long userId : userIds) {
                Product product = new Product("flat " + userId, userId, null);
                insert(product);
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public Product insert(Product product) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(CREATE_PRODUCT)) {

            pstmt.setString(1, product.getName());
            pstmt.setLong(2, product.getOwnerId());

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    long productId = resultSet.getLong(1);
                    Product productFromDB = new Product(product.getName(), product.getOwnerId(), productId);
                    return productFromDB;
                } else {
                    throw new SQLException("Ошибка при вставке товара, " + product);
                }
            }
        }
    }

    public List<Product> getUserProducts(Long userId) throws SQLException {
        List<Product> userProducts = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_PRODUCTS_BY_OWNER)) {

            pstmt.setLong(1, userId);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
                    long productId = resultSet.getLong(1);
                    String productName = resultSet.getString(2);
                    long ownerId = resultSet.getLong(3);

                    Product product = new Product(productName, ownerId, productId);
                    userProducts.add(product);
                }
            }
        }
        return userProducts;
    }

    public Product getProductById(Long productId) throws SQLException {

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_PRODUCTS_BY_ID)) {

            pstmt.setLong(1, productId);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    String productName = resultSet.getString(2);
                    long ownerId = resultSet.getLong(3);

                    Product product = new Product(productName, ownerId, productId);
                    return product;
                }
            }
        }
        return null;
    }

    public void updateProduct(Product product) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_PRODUCT)) {

            pstmt.setLong(1, product.getOwnerId());
            pstmt.setString(2, product.getName());
            pstmt.setLong(3, product.getId());

            pstmt.executeUpdate();
        }
    }
}
