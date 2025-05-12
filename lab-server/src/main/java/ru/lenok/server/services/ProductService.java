package ru.lenok.server.services;

import ru.lenok.common.Product;
import ru.lenok.server.daos.DBConnector;
import ru.lenok.server.daos.ProductDAO;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class ProductService {
    private final ProductDAO productDAO;
    private final DataSource ds;

    public ProductService(ProductDAO productDAO, DBConnector dbConnector) throws SQLException {
        this.productDAO = productDAO;
        ds = dbConnector.getDatasource();
    }

    public void registerProduct(String productName, Long ownerId) throws SQLException {
        Product product = new Product(productName, ownerId, null);
        productDAO.insert(product);
    }

    public List<Product> getUserProducts(Long userId) throws SQLException {
        return productDAO.getUserProducts(userId);
    }
}
