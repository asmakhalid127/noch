package com.noch.service;

import com.noch.dao.ProductDAO;
import com.noch.dao.impl.ProductDAOImpl;

import java.util.List;

public class ProductService {

    private final ProductDAO productDAO = new ProductDAOImpl();

    public List<String[]> getAllProducts() {
        return productDAO.getAllProducts();
    }

    public String[] getProductById(String id) {
        return productDAO.getProductById(id);
    }
}