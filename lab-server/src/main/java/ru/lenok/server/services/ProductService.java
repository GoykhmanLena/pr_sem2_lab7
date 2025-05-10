package ru.lenok.server.services;

import ru.lenok.common.Product;
import ru.lenok.server.daos.DBConnector;
import ru.lenok.server.daos.ProductDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ProductService {
    private final ProductDAO productDAO;
    private final Connection connection;
    public ProductService(ProductDAO productDAO, DBConnector dbConnector) {
        this.productDAO = productDAO;
        connection = dbConnector.getConnection();
    }

    public void registerProduct(String productName, Long ownerId) throws SQLException {
        Product product = new Product(productName, ownerId, null);
        try {
            productDAO.insert(product);
            connection.commit();
        } catch (SQLException e){
            connection.rollback();
        }
    }

    public List<Product> getUserProducts(Long userId) throws SQLException {
        return productDAO.getUserProducts(userId);
    }
}
