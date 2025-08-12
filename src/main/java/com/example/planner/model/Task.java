package com.example.planner.model;

import java.time.LocalDate;
import java.time.LocalTime;
import com.example.planner.model.Course;

public class Task {
    private Course course;
    private boolean complete = false;
    private String name;
    private String description;
    private LocalDate dueDate;
    private double priority;

    public Task(String name, String description, LocalDate dueDate) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
    }
    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Course getCourse() {
        return course;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }



    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }
}
