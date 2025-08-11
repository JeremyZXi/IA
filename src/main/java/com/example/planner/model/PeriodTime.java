// src/main/java/com/example/planner/model/PeriodTime.java
package com.example.planner.model;

import java.time.LocalTime;

public class PeriodTime {
    private int periodNumber;
    private LocalTime start;
    private LocalTime end;

    public PeriodTime() {
    }

    public PeriodTime(int periodNumber, LocalTime start, LocalTime end) {
        this.periodNumber = periodNumber;
        this.start = start;
        this.end = end;
    }

    public int getPeriodNumber() {
        return periodNumber;
    }

    public void setPeriodNumber(int periodNumber) {
        this.periodNumber = periodNumber;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }
}
