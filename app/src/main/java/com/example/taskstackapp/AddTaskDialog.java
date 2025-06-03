package com.example.taskstackapp;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.taskstackapp.databinding.DialogAddTaskBinding;

public class AddTaskDialog extends DialogFragment {

    private DialogAddTaskBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Task editingTask;
    private boolean isEditing = false;

    // Pastikan class Task mengimplement Serializable agar bisa dipakai di Bundle
    public static AddTaskDialog newInstance(Task task) {
        AddTaskDialog dialog = new AddTaskDialog();
        Bundle args = new Bundle();
        args.putSerializable("task", task);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (getArguments() != null) {
            editingTask = (Task) getArguments().getSerializable("task");
            isEditing = editingTask != null;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogAddTaskBinding.inflate(getLayoutInflater());

        setupDialog();
        setupClickListeners();

        if (isEditing) {
            populateFields();
        }

        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();
    }

    private void setupDialog() {
        binding.tvDialogTitle.setText(isEditing ? "Edit Task" : "Add New Task");
        binding.btnSave.setText(isEditing ? "Update" : "Save");
    }

    private void setupClickListeners() {
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnSave.setOnClickListener(v -> saveTask());
    }

    private void populateFields() {
        if (editingTask != null) {
            binding.etTaskTitle.setText(editingTask.getTitle());
            binding.etTaskDescription.setText(editingTask.getDescription());

            switch (editingTask.getPriority().toLowerCase()) {
                case "high":
                    binding.rbHigh.setChecked(true);
                    break;
                case "medium":
                    binding.rbMedium.setChecked(true);
                    break;
                case "low":
                    binding.rbLow.setChecked(true);
                    break;
            }
        }
    }

    private void saveTask() {
        String title = binding.etTaskTitle.getText().toString().trim();
        String description = binding.etTaskDescription.getText().toString().trim();
        String priority = getSelectedPriority();

        if (TextUtils.isEmpty(title)) {
            binding.etTaskTitle.setError("Title is required");
            return;
        }

        binding.btnSave.setEnabled(false);
        binding.btnSave.setText(isEditing ? "Updating..." : "Saving...");

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference tasksRef = mDatabase.child("tasks").child(userId);

        if (isEditing) {
            editingTask.setTitle(title);
            editingTask.setDescription(description);
            editingTask.setPriority(priority);

            tasksRef.child(editingTask.getId()).setValue(editingTask)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Task updated successfully", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                        binding.btnSave.setEnabled(true);
                        binding.btnSave.setText("Update");
                    });
        } else {
            Task newTask = new Task(title, description, priority, userId);

            tasksRef.push().setValue(newTask)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to add task", Toast.LENGTH_SHORT).show();
                        binding.btnSave.setEnabled(true);
                        binding.btnSave.setText("Save");
                    });
        }
    }

    private String getSelectedPriority() {
        if (binding.rbHigh.isChecked()) {
            return "High";
        } else if (binding.rbLow.isChecked()) {
            return "Low";
        } else {
            return "Medium";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
