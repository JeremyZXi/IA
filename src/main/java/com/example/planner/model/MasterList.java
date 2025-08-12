package com.example.planner.model;
import java.util.ArrayList;
import java.util.List;
public class MasterList {
    private List<TaskList> master = new ArrayList();

    public void addDay(TaskList day){
        this.master.add(day);
    }
    public void removeDay(TaskList day){
        this.master.remove(day);
    }
    public void removeDay(int index){
        this.master.remove(index);
    }
}
