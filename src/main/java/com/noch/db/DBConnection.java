package com.noch.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:sqlite:noch_db.sqlite";
    private static Connection instance;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(URL);
            instance.createStatement().execute("PRAGMA foreign_keys = ON;");
        }
        return instance;
    }
}
