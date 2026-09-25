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
        String createSessionsTable = """
            CREATE TABLE IF NOT EXISTS sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                source_folder TEXT NOT NULL,
                destination_folder TEXT NOT NULL,
                started_at TEXT NOT NULL
            )
            """;

        String createMoveHistoryTable = """
            CREATE TABLE IF NOT EXISTS move_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER NOT NULL,
                original_path TEXT NOT NULL,
                new_path TEXT NOT NULL,
                category TEXT NOT NULL,
                moved_at TEXT NOT NULL,
                status TEXT NOT NULL,
                FOREIGN KEY (session_id) REFERENCES sessions(id)
            )
            """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute(createSessionsTable);
            stmt.execute(createMoveHistoryTable);
            System.out.println("Database ready (sessions + move_history).");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}