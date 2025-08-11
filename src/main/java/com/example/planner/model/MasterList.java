package com.example.planner.model;
import java.util.ArrayList;
import java.util.List;
public class MasterList {
    private List<Day> master = new ArrayList();

    public void addDay(Day day){
        this.master.add(day);
    }
    public void removeDay(Day day){
        this.master.remove(day);
    }
    public void removeDay(int index){
        this.master.remove(index);
    }
}
