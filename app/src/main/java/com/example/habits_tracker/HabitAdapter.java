package com.example.habits_tracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitHolder> {

    private List<Habit> habits;
    private OnItemClickListener listener;

    public HabitAdapter(List<Habit> habits) {
        this.habits = habits;
    }
    public void setHabits(List<Habit> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public HabitHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_item, parent, false);
        return new HabitHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull HabitHolder holder, int position) {
        Habit currentHabit = habits.get(position);
        holder.textViewDescription.setText(currentHabit.getDescription());
        holder.textViewTime.setText(String.format("%02d:%02d", currentHabit.getHour(), currentHabit.getMinute()));
        holder.switchEnabled.setChecked(currentHabit.isEnabled());
        holder.switchEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (listener != null) {
                    currentHabit.setEnabled(isChecked);
                    listener.onSwitchClick(currentHabit);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }
    public class HabitHolder extends RecyclerView.ViewHolder {
        private TextView textViewDescription;
        private TextView textViewTime;
        private SwitchCompat switchEnabled;

        public HabitHolder(@NonNull View itemView) {
            super(itemView);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            textViewTime = itemView.findViewById(R.id.text_view_time);
            switchEnabled = itemView.findViewById(R.id.switch_enabled);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(habits.get(position));
                    }
                }
            });
        }
    }
    public interface OnItemClickListener {
        void onItemClick(Habit habit);
        void onSwitchClick(Habit habit);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}