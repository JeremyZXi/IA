package com.example.planner.ui;

import com.example.planner.data.StorageManager;
import com.example.planner.model.*;
import java.io.File;


import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.util.*;

import java.io.FileReader;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;

import com.example.planner.data.ConfigManager;
import com.example.planner.model.PeriodTime;
public class PlannerController {

    // Nav (left narrow)
    @FXML private Button listBtn, matrixBtn, calendarBtn, helpBtn;

    // Left main
    @FXML private Label greetingLabel, dateLabel;
    @FXML private VBox classListVBox;

    // Center
    @FXML private Label subjectTitleLabel, periodInfoLabel;
    @FXML private Button autoPlanBtn;
    @FXML private VBox taskListVBox, pendingListVBox;

    // Right (detail)
    @FXML private CheckBox detailDoneCheck;
    @FXML private Label detailMetaLabel, detailTitleLabel;
    @FXML private VBox attachmentListVBox;
    @FXML private Button addAttachmentBtn;
    //add task
    @FXML private DatePicker picker;
    @FXML private Button addTaskBtn;
    @FXML private TextField quickTaskField;
    @FXML private  TextArea taskDescription;
    @FXML private VBox detailPane;
    private LocalDate date = LocalDate.parse("2025-08-10");//LocalDate.now();
    private TaskList regularTasks;
    private  TaskList pendingTasks;
    private Task currentDetailTask;

    private MdTextArea detailNote = new MdTextArea();

    //lookup table for detail pane
    private final Map<Task, TaskCard> taskCardMap = new HashMap<>();

    @FXML
    private void initialize() throws Exception {
        //load data
        UserSettings settings = ConfigManager.load();
        int periodsPerDay = settings.getPeriodsPerDay();
        int daysPerCycle = settings.getDaysPerCycle();
        List<List<String>> courseMatrix = settings.getCourseMatrix();
        List<PeriodTime> periodTimes = settings.getPeriods();
        //load course matrix
        List<List<Course>> schedule = new ArrayList<>();
        for(int i = 0;i<daysPerCycle;i++){
            List<Course> day = new ArrayList<>();
            for(int j = 0; j<periodsPerDay;j++){
                day.add(new Course(courseMatrix.get(i).get(j),(char)(65+i),periodTimes.get(j)));
            }
            schedule.add(day);
        }

        //add the correct courses to display
        List<Course> courseToday = new ArrayList<>();
        if (letterDate(date) != '0'){
            courseToday = schedule.get(letterDate2Index(letterDate(date)));
        } else {
            courseToday.add(new Course("Break",'0',new PeriodTime(0, LocalTime.parse("00:00"),LocalTime.parse("23:59"))));
        }
        for(Course course : courseToday){
            addClassCard(course.getCourseName(),"Period "+course.getPeriodTime().getPeriodNumber() +" | "+course.getPeriodTime().getStart().toString()+"~"+course.getPeriodTime().getEnd().toString());
        }

        if(StorageManager.storageExists()){
            //TODO:should create a new manager for pending task or somehow combine them
            regularTasks = new TaskList();
            pendingTasks = new TaskList();

        }else {
            regularTasks = new TaskList();
            pendingTasks = new TaskList();
        }

        // Header text
        greetingLabel.setText("Hi, "+settings.getDisplayName());
        dateLabel.setText(humanDate(date));

        detailPane.getChildren().add(detailNote);
        //listen
        detailDoneCheck.selectedProperty().addListener((obs, was, isNow) -> {
            if (currentDetailTask != null) {
                currentDetailTask.setComplete(isNow);
                TaskCard card = taskCardMap.get(currentDetailTask);
                System.out.println(isNow);
                if (card != null) {
                    card.applyCompletionStyle(isNow); // you'll need this method in TaskCard
                    if (isNow) {
                        // move completed card to bottom
                        pendingListVBox.getChildren().remove(card);
                        pendingListVBox.getChildren().add(card);
                    }
                }
            }
        });



    }
    @FXML
    public void addPendingTask(){
        String name = quickTaskField.getText();
        String description = taskDescription.getText();
        LocalDate dueDate = picker.getValue();
        Task task = new Task(name,description,dueDate);
        pendingTasks.addTask(task);
        addPendingTaskCard(task);
    }

    /* ========== Public helpers to add dynamic content ========== */

    public void setCenterHeader(String subject, String periodInfo) {
        subjectTitleLabel.setText(subject);
        periodInfoLabel.setText(periodInfo);
    }

    public void addClassCard(String title, String subtitle) {
        // A class "card" as a Button with title+subtitle (clickable)
        VBox text = new VBox(2);
        Label t = new Label(title);
        t.setStyle("-fx-font-weight:bold;");
        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill:#666;");
        text.getChildren().addAll(t, s);

        Button card = new Button();
        card.setGraphic(text);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(56);
        card.setStyle("-fx-background-color:white; -fx-border-color:#cccccc; -fx-alignment:BASELINE_LEFT; -fx-padding:8;");
        HBox.setHgrow(card, Priority.ALWAYS);

        card.setOnAction(e -> {
            // change center header when a class is selected
            setCenterHeader(title, subtitle);
            System.out.println("Selected class: " + title);
        });

        classListVBox.getChildren().add(card);
    }



    public void addPendingTaskCard(Task task) {
        TaskCard card = new TaskCard(task);
        taskCardMap.put(task, card);
        card.setOnOpen(() -> setDetail(task));


        card.setOnPersist(t -> {
            // StorageManager.save(...); // persistence storage
        });
        card.setOnMoveToBottom(() -> {
            pendingListVBox.getChildren().remove(card);
            pendingListVBox.getChildren().add(card);
        });

        pendingListVBox.getChildren().add(card);
    }


    private void saveCurrentDetail() {
        if (currentDetailTask != null && detailNote.isDirty()) {
            currentDetailTask.setDescription(detailNote.getText());
            // TODO:persistant memory goes here
            // StorageManager.saveTask(currentDetailTask);
            detailNote.clearDirty();
        }
    }

    public void setDetail(Task task) {
        saveCurrentDetail();

        this.currentDetailTask = task;
        detailDoneCheck.setSelected(task.isComplete());
        detailMetaLabel.setText(task.getDueDate() != null ? task.getDueDate().toString() : "");
        detailTitleLabel.setText(task.getName());
        //detailBodyLabel.setText(task.getDescription() != null ? task.getDescription() : "");
        detailNote.setInputSpace(task.getDescription() != null ? task.getDescription() : "");
    }



    /* ========== Internals / stubs ========== */

    private void chooseAttachmentInto(Node containerRow, Label nameLabel) {
        File file = new FileChooser().showOpenDialog(containerRow.getScene().getWindow());
        if (file != null) nameLabel.setText(file.getName());
    }

    private String humanDate(LocalDate d) {
        String dow = d.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String mon = d.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return dow + " " + mon + " " + d.getDayOfMonth() + "," +letterDate(d)+" day";
    }
    //TODO: use better search algorithm
    /**convert localdate to letter day using linear search
     * @param d java LocalDate
     * @return letter date*/
    private char letterDate(LocalDate d){
        char letter = '0';
        List<String[]> data = readCSV("test data/letter_day_calendar.csv");
        for(String[] row:data){
            if(row[0].equals(d.toString())){
                letter = row[2].charAt(0);
            }
        }
        return letter;
    }
    private int letterDate2Index(char letter){
        if(letter == '0'){
            return -1;
        } else {
            return (int)letter - 65;
        }
    }
    /** this method reads CSV that maps letter date to actual date*/
    private List<String[]> readCSV(String file) {
        List<String[]> allData = null;
        try {
            FileReader filereader = new FileReader(file);
            CSVReader csvReader = new CSVReaderBuilder(filereader)
                    .withSkipLines(1)
                    .build();
            allData = csvReader.readAll();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return allData;
    }

}
