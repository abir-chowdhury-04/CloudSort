package com.abir_2307055.cloudsort.cloudsort.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class BaseDao {

    protected void executeUpdate(String sql, Object... parameters) throws SQLException {
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < parameters.length; i++) {
                ps.setObject(i + 1, parameters[i]);
            }

            ps.executeUpdate();
        }
    }
}