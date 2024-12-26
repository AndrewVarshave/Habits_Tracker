package com.example.habits_tracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String description;
    private int hour;
    private int minute;
    private boolean enabled;

    public Habit(String description, int hour, int minute, boolean enabled) {
        this.description = description;
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }
    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}