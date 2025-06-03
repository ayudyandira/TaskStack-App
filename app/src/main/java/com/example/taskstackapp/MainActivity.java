package com.example.taskstackapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.taskstackapp.TaskAdapter;
import com.example.taskstackapp.databinding.ActivityMainBinding;
import com.example.taskstackapp.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setupToolbar();
        setupRecyclerView();
        setupFAB();
        loadTasks();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Tasks");
        }
    }

    private void setupRecyclerView() {
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(taskAdapter);
    }

    private void setupFAB() {
        binding.fabAdd.setOnClickListener(v -> showAddTaskDialog());
    }

    private void loadTasks() {
        String userId = mAuth.getCurrentUser().getUid();

        mDatabase.child("tasks").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        taskList.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Task task = snapshot.getValue(Task.class);
                            if (task != null) {
                                task.setId(snapshot.getKey());
                                taskList.add(task);
                            }
                        }

                        // Sort tasks by timestamp (newest first)
                        Collections.sort(taskList, (t1, t2) ->
                                Long.compare(t2.getTimestamp(), t1.getTimestamp()));

                        taskAdapter.notifyDataSetChanged();

                        // Show/hide empty state
                        if (taskList.isEmpty()) {
                            binding.recyclerView.setVisibility(View.GONE);
                            binding.tvEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            binding.recyclerView.setVisibility(View.VISIBLE);
                            binding.tvEmptyState.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddTaskDialog() {
        AddTaskDialog dialog = new AddTaskDialog();
        dialog.show(getSupportFragmentManager(), "AddTaskDialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onTaskClick(Task task) {
        AddTaskDialog dialog = AddTaskDialog.newInstance(task);
        dialog.show(getSupportFragmentManager(), "EditTaskDialog");
    }

    @Override
    public void onTaskLongClick(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTask(task))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onTaskCheckedChanged(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        updateTask(task);
    }

    private void updateTask(Task task) {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("tasks").child(userId).child(task.getId())
                .setValue(task)
                .addOnSuccessListener(aVoid -> {
                    // Task updated successfully
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to update task", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteTask(Task task) {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("tasks").child(userId).child(task.getId())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                });
    }
}
