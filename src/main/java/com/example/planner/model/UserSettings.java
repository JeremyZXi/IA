// src/main/java/com/example/planner/model/UserSettings.java
package com.example.planner.model;

import java.util.ArrayList;
import java.util.List;

public class UserSettings {
    private String displayName;
    private int periodsPerDay;
    private int daysPerCycle;
    private List<PeriodTime> periods = new ArrayList<>();

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public int getPeriodsPerDay() { return periodsPerDay; }
    public void setPeriodsPerDay(int periodsPerDay) { this.periodsPerDay = periodsPerDay; }
    public int getDaysPerCycle() { return daysPerCycle; }
    public void setDaysPerCycle(int daysPerCycle) { this.daysPerCycle = daysPerCycle; }
    public List<PeriodTime> getPeriods() { return periods; }
    public void setPeriods(List<PeriodTime> periods) { this.periods = periods; }
}
