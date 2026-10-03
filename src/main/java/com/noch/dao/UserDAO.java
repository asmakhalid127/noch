package com.noch.dao;

import com.noch.models.User;

public interface UserDAO {
    User findByEmailAndPassword(String email, String password);
}
