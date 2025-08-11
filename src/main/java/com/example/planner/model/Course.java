package com.example.planner.model;

public class Course {
    private String courseName;
    private char courseLetterDate;

    private  PeriodTime periodTime;

    public Course(String courseName,char courseLetterDate,PeriodTime periodTime){
        this.courseName = courseName;
        this.courseLetterDate=courseLetterDate;
        this.periodTime = periodTime;
    }
    public String getCourseName(){
        return  courseName;
    }
    public char getCourseLetterDate(){
        return courseLetterDate;
    }
    public PeriodTime getPeriodTime(){
        return periodTime;
    }

}
