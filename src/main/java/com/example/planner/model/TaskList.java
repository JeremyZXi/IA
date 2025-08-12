package com.example.planner.model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskList {
    private LocalDate date;
    private List<Task> taskList = new ArrayList();

    public TaskList() {

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
