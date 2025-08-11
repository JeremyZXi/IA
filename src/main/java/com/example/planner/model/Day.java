package com.example.planner.model;

import java.util.ArrayList;
import java.util.List;

public class Day {
    private List<TaskList> day = new ArrayList();

    public Day(List<TaskList> day) {
        this.day = day;
    }
    public void addTaskList(TaskList taskList){
        this.day.add(taskList);
    }
    public void removeTaskList(TaskList taskList){
        this.day.remove(taskList);
    }
    public void removeTaskList(int index){
        this.day.remove(index);
    }

}
