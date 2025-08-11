package com.example.planner.model;

import java.time.LocalTime;
import com.example.planner.model.Course;

public class Task {
    private String name;
    private String description;
    private LocalTime dueDate;
    private Course parent;
    private double priority;

    public Task(String name, String description, LocalTime dueDate, Course parent, double priority) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalTime dueDate) {
        this.dueDate = dueDate;
    }

    public Course getParent() {
        return parent;
    }

    public void setParent(Course parent) {
        this.parent = parent;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }
}
