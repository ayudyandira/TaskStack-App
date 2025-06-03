package com.example.taskstackapp;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskstackapp.databinding.ItemTaskBinding;
import com.example.taskstackapp.Task;
import com.example.taskstackapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
        void onTaskCheckedChanged(Task task, boolean isChecked);
    }

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private ItemTaskBinding binding;

        public TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Task task) {
            binding.tvTitle.setText(task.getTitle());
            binding.tvDescription.setText(task.getDescription());
            binding.cbCompleted.setChecked(task.isCompleted());

            // Set priority
            binding.tvPriority.setText(task.getPriority().toUpperCase());
            setPriorityColor(task.getPriority());

            // Set date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            binding.tvDate.setText(sdf.format(new Date(task.getTimestamp())));

            // Handle completed state
            if (task.isCompleted()) {
                binding.tvTitle.setPaintFlags(binding.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvTitle.setAlpha(0.6f);
                binding.tvDescription.setAlpha(0.6f);
            } else {
                binding.tvTitle.setPaintFlags(binding.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.tvTitle.setAlpha(1.0f);
                binding.tvDescription.setAlpha(1.0f);
            }

            // Click listeners
            binding.getRoot().setOnClickListener(v -> listener.onTaskClick(task));
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onTaskLongClick(task);
                return true;
            });

            binding.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                listener.onTaskCheckedChanged(task, isChecked);
            });

            binding.ivOptions.setOnClickListener(v -> showOptionsMenu(v, task));
        }

        private void setPriorityColor(String priority) {
            int colorRes;
            switch (priority.toLowerCase()) {
                case "high":
                    colorRes = R.color.priority_high;
                    break;
                case "medium":
                    colorRes = R.color.priority_medium;
                    break;
                case "low":
                    colorRes = R.color.priority_low;
                    break;
                default:
                    colorRes = R.color.priority_medium;
            }

            binding.tvPriority.setBackgroundTintList(
                    ContextCompat.getColorStateList(itemView.getContext(), colorRes));
        }

        private void showOptionsMenu(View view, Task task) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenuInflater().inflate(R.menu.task_options_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit) {
                    listener.onTaskClick(task);
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    listener.onTaskLongClick(task);
                    return true;
                }
                return false;
            });

            popup.show();
        }
    }
}
