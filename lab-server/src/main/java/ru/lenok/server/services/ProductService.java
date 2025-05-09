package ru.lenok.server.services;

import ru.lenok.common.Product;
import ru.lenok.server.daos.LabWorkDAO;
import ru.lenok.server.daos.OfferDAO;
import ru.lenok.server.daos.ProductDAO;

import java.sql.SQLException;
import java.util.List;

public class ProductService {
    private final ProductDAO productDAO;
    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public void registerProduct(String productName, Long ownerId) throws SQLException {
        Product product = new Product(productName, ownerId, null);
        productDAO.insert(product);
    }

    public List<Product> getUserProducts(Long userId) throws SQLException {
        return productDAO.getUserProducts(userId);
    }
}
