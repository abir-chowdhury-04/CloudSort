package com.abir_2307055.cloudsort.cloudsort.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:sqlite:cloudsort-history.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        String sql = """
                CREATE TABLE IF NOT EXISTS move_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    original_path TEXT NOT NULL,
                    new_path TEXT NOT NULL,
                    category TEXT NOT NULL,
                    moved_at TEXT NOT NULL,
                    status TEXT NOT NULL
                )
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Move history database ready.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}