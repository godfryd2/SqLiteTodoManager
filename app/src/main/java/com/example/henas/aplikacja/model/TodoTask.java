package com.example.henas.aplikacja.model;

/**
 * Created by Henas on 07.11.2016.
 */

public class TodoTask {
    private long id;
    private String description;
    private String date;
    private boolean completed;

    private String status;

    public TodoTask(long id, String description, String date, boolean completed, String status) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.completed = completed;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() { return date; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
