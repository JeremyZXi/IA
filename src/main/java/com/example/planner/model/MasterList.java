package com.example.planner.model;

import java.util.ArrayList;
import java.util.List;

public class MasterList {
    private final List<TaskList> master = new ArrayList();

    /**
     * constructor and for jackson
     */
    public MasterList() {
    }

    public List<TaskList> getMaster() {
        return master;
    }

    public void addTaskList(TaskList taskList) {
        this.master.add(taskList);
    }

    public void removeTaskList(TaskList taskList) {
        this.master.remove(taskList);
    }

    public void removeTaskList(int index) {
        this.master.remove(index);
    }
}
