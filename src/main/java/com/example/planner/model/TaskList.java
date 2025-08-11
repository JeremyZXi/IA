package com.example.planner.model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskList {
    private Course course;
    private LocalDate dueDate;
    private List<Task> taskList = new ArrayList();

    public TaskList(Course course, LocalDate dueDate) {
        this.course = course;
        this.dueDate = dueDate;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }
    public void addTask(Task task){
        this.taskList.add(task);
    }
    public void removeTask(int index){
        this.taskList.remove(index);
    }
}
