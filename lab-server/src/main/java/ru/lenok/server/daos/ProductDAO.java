package ru.lenok.server.daos;

import ru.lenok.common.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ru.lenok.server.daos.SQLQueries.*;

public class ProductDAO extends AbstractDAO {
    public ProductDAO(Set<Long> userIds, DBConnector dbConnector, boolean dbReinit) throws SQLException {
        super(dbConnector.getDatasource());
        init(userIds, dbReinit);
    }

    private void init(Set<Long> userIds, boolean dbReinit) throws SQLException {
        initScheme(dbReinit);
        if (dbReinit) {
            persistInitialState(userIds);
        }
    }

    private void initScheme(boolean reinitDB) throws SQLException {
        try (Connection connection = ds.getConnection(); Statement stmt = connection.createStatement()) {
            if (reinitDB) {
                stmt.executeUpdate(DROP_ALL_PRODUCT.t());
            }
            stmt.executeUpdate(CREATE_SEQUENCE_PRODUCT.t());
            stmt.executeUpdate(CREATE_TABLE_PRODUCT.t());
            stmt.executeUpdate(CREATE_INDEX_PRODUCT.t());
        }
    }

    private void persistInitialState(Set<Long> userIds) throws SQLException {
        for (Long userId : userIds) {
            Product product = new Product("flat " + userId, userId, null);
            insert(product);
        }
    }

    public Product insert(Product product) throws SQLException {
        try (Connection connection = ds.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(CREATE_PRODUCT.t())) {
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
        try (Connection connection = ds.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SELECT_PRODUCTS_BY_OWNER.t())) {
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
        try (Connection connection = ds.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SELECT_PRODUCTS_BY_ID.t())) {
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

    public void updateProduct(Product product, Connection connection) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_PRODUCT.t())) {
            pstmt.setLong(1, product.getOwnerId());
            pstmt.setString(2, product.getName());
            pstmt.setLong(3, product.getId());
            pstmt.executeUpdate();
        }
    }
}
