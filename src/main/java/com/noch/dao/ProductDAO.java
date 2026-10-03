package com.noch.dao;

import java.util.List;

public interface ProductDAO {
    List<String[]> getAllProducts();
    String[] getProductById(String id);
}
