package com.abir_2307055.cloudsort.cloudsort.model;

public class MoveRecord {
    private final long id;
    private final long sessionId;
    private final String originalPath;
    private final String newPath;
    private final String category;
    private final String movedAt;
    private final String status;

    public MoveRecord(long id, long sessionId, String originalPath, String newPath,
                      String category, String movedAt, String status) {
        this.id = id;
        this.sessionId = sessionId;
        this.originalPath = originalPath;
        this.newPath = newPath;
        this.category = category;
        this.movedAt = movedAt;
        this.status = status;
    }

    public long getId() { return id; }
    public long getSessionId() { return sessionId; }
    public String getOriginalPath() { return originalPath; }
    public String getNewPath() { return newPath; }
    public String getCategory() { return category; }
    public String getMovedAt() { return movedAt; }
    public String getStatus() { return status; }
}