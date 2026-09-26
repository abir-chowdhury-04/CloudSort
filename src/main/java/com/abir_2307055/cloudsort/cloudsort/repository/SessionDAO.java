package com.abir_2307055.cloudsort.cloudsort.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class SessionDAO extends BaseDao  {

    public long insertSession(String sourceFolder, String destinationFolder) throws SQLException {
        String sql = "INSERT INTO sessions(source_folder, destination_folder, started_at) VALUES(?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sourceFolder);
            ps.setString(2, destinationFolder);
            ps.setString(3, LocalDateTime.now().toString());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return -1;
    }
}