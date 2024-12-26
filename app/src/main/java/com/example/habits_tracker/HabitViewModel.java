package com.example.habits_tracker;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HabitViewModel extends AndroidViewModel {
    private HabitDao habitDao;
    private LiveData<List<Habit>> allHabits;
    private ExecutorService executorService;

    public HabitViewModel(@NonNull Application application) {
        super(application);
        HabitDatabase database = HabitDatabase.getInstance(application);
        habitDao = database.habitDao();
        allHabits = habitDao.getAllHabits();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Habit>> getAllHabits() {
        return allHabits;
    }
    public void insert(Habit habit) {
        executorService.execute(() -> habitDao.insert(habit));
    }

    public void update(Habit habit) {
        executorService.execute(() -> habitDao.update(habit));
    }

    public void delete(Habit habit) {
        executorService.execute(() -> habitDao.delete(habit));
    }

    public LiveData<Integer> checkHabitExists(String description, int hour, int minute) {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executorService.execute(() -> {
            int count = habitDao.checkHabitExists(description, hour, minute);
            result.postValue(count);
        });
        return  result;
    }
}
