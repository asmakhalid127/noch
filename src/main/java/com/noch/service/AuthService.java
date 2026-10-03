package com.noch.service;

import com.noch.dao.UserDAO;
import com.noch.dao.impl.UserDAOImpl;
import com.noch.models.User;

public class AuthService {

    private final UserDAO userDAO = new UserDAOImpl();

    public User login(String email, String password) {
        return userDAO.findByEmailAndPassword(email, password);
    }
}
