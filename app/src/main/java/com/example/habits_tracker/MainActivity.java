package com.example.habits_tracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements HabitAdapter.OnItemClickListener {
    private HabitViewModel habitViewModel;
    private RecyclerView recyclerView;
    private HabitAdapter adapter;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Permission for notifications is not granted", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fabAddHabit = findViewById(R.id.fab_add_habit);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        List<Habit> initialHabits = new ArrayList<>();
        adapter = new HabitAdapter(initialHabits);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);
        habitViewModel.getAllHabits().observe(this, habits -> {
            adapter.setHabits(habits);
            adapter.notifyDataSetChanged();
        });

        fabAddHabit.setOnClickListener(view -> showAddHabitDialog(null));
        checkNotificationPermissions();
    }
    private void checkNotificationPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void showAddHabitDialog(Habit habit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(habit == null ? "Add New Habit" : "Edit Habit");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText descriptionEditText = new EditText(this);
        descriptionEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        descriptionEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});
        descriptionEditText.setHint("Description");
        layout.addView(descriptionEditText);
        final int[] selectedHour = {0};
        final int[] selectedMinute = {0};
        final EditText timeEditText = new EditText(this);
        timeEditText.setHint("Time");
        timeEditText.setInputType(InputType.TYPE_NULL);
        timeEditText.setFocusable(false);
        timeEditText.setClickable(true);
        timeEditText.setOnClickListener(v -> {
            showTimePickerDialog(selectedHour, selectedMinute, timeEditText);
        });
        layout.addView(timeEditText);
        builder.setView(layout);

        if (habit != null) {
            descriptionEditText.setText(habit.getDescription());
            selectedHour[0] = habit.getHour();
            selectedMinute[0] = habit.getMinute();
            timeEditText.setText(String.format("%02d:%02d", selectedHour[0], selectedMinute[0]));
        }

        builder.setPositiveButton(habit == null ? "Add" : "Update", (dialog, which) -> {
            String description = descriptionEditText.getText().toString().trim();
            if(description.isEmpty() || timeEditText.getText().toString().isEmpty()){
                Toast.makeText(MainActivity.this, "Description or time is not filled", Toast.LENGTH_SHORT).show();
                return;
            }

            if (habit == null) {
                habitViewModel.checkHabitExists(description, selectedHour[0], selectedMinute[0])
                        .observe(this, count -> {
                            if (count > 0) {
                                Toast.makeText(this, "Habit with such description and time exists", Toast.LENGTH_SHORT).show();
                            } else {
                                Habit newHabit = new Habit(description, selectedHour[0], selectedMinute[0], true);
                                habitViewModel.insert(newHabit);
                                setAlarm(newHabit);
                            }
                        });
            } else {
                if(!habit.getDescription().equals(description) || habit.getHour() != selectedHour[0] || habit.getMinute() != selectedMinute[0]) {
                    habitViewModel.checkHabitExists(description, selectedHour[0], selectedMinute[0])
                            .observe(this, count -> {
                                if (count > 0) {
                                    Toast.makeText(this, "Habit with such description and time exists", Toast.LENGTH_SHORT).show();
                                } else {
                                    habit.setDescription(description);
                                    habit.setHour(selectedHour[0]);
                                    habit.setMinute(selectedMinute[0]);
                                    habitViewModel.update(habit);
                                    setAlarm(habit);
                                }
                            });
                }
            }
        });

        if (habit != null) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                showDeleteConfirmationDialog(habit);
                dialog.dismiss(); // Закрываем диалог редактирования
            });
        }


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    private void showTimePickerDialog(int[] selectedHour, int[] selectedMinute, EditText timeEditText) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour[0] = hourOfDay;
                    selectedMinute[0] = minute;
                    timeEditText.setText(String.format("%02d:%02d", hourOfDay, minute));
                },
                selectedHour[0],
                selectedMinute[0],
                true
        );
        timePickerDialog.show();
    }
    @Override
    public void onItemClick(Habit habit) {
        showAddHabitDialog(habit);
    }

    @Override
    public void onSwitchClick(Habit habit) {
        habitViewModel.update(habit);
        if(habit.isEnabled()){
            setAlarm(habit);
        } else {
            cancelAlarm(habit);
        }
    }

    private void setAlarm(Habit habit) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("HABIT_DESCRIPTION", habit.getDescription());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, habit.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, habit.getHour());
        calendar.set(Calendar.MINUTE, habit.getMinute());
        calendar.set(Calendar.SECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
    private void cancelAlarm(Habit habit) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, habit.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    private void showDeleteConfirmationDialog(Habit habit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Habit");
        builder.setMessage("Are you sure you want to delete this habit?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
                    habitViewModel.delete(habit);
                    cancelAlarm(habit);
                    Toast.makeText(this, "Habit deleted", Toast.LENGTH_SHORT).show();
                }
        );
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}