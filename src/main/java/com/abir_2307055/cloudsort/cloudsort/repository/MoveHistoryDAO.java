package com.abir_2307055.cloudsort.cloudsort.repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.abir_2307055.cloudsort.cloudsort.model.MoveRecord;

public class MoveHistoryDAO {

    // CREATE
    public long insertMove(String originalPath, String newPath, String category) throws SQLException {
        String sql = "INSERT INTO move_history(original_path, new_path, category, moved_at, status) "
                + "VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, originalPath);
            ps.setString(2, newPath);
            ps.setString(3, category);
            ps.setString(4, LocalDateTime.now().toString());
            ps.setString(5, "pending");
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return -1;
    }

    // READ
    public List<MoveRecord> getAllMoves() throws SQLException {
        String sql = "SELECT id, original_path, new_path, category, moved_at, status "
                + "FROM move_history ORDER BY moved_at DESC";
        List<MoveRecord> records = new ArrayList<>();

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                records.add(new MoveRecord(
                        rs.getLong("id"),
                        rs.getString("original_path"),
                        rs.getString("new_path"),
                        rs.getString("category"),
                        rs.getString("moved_at"),
                        rs.getString("status")
                ));
            }
        }
        return records;
    }

    // UPDATE
    public void updateStatus(long id, String newStatus) throws SQLException {
        String sql = "UPDATE move_history SET status = ? WHERE id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    // DELETE
    public void clearHistory() throws SQLException {
        String sql = "DELETE FROM move_history";
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}