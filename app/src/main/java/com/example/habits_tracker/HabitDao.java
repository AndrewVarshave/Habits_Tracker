package com.example.habits_tracker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface HabitDao {
    @Insert
    void insert(Habit habit);

    @Update
    void update(Habit habit);

    @Delete
    void delete(Habit habit);

    @Query("SELECT * FROM habits")
    LiveData<List<Habit>> getAllHabits();

    @Query("SELECT COUNT(*) FROM habits WHERE description = :description AND hour = :hour AND minute = :minute")
    int checkHabitExists(String description, int hour, int minute);
}
