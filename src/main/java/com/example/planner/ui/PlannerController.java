package com.example.planner.ui;

import com.example.planner.data.StorageManager;
import com.example.planner.data.UserDataManager;
import com.example.planner.model.*;

import java.io.File;
import java.awt.Toolkit;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
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
    @FXML
    private Button listBtn, matrixBtn, calendarBtn, helpBtn;

    // Left main
    @FXML
    private Label greetingLabel, dateLabel;


    // Center
    @FXML
    private Label subjectTitleLabel, periodInfoLabel;
    @FXML
    private Button autoPlanBtn;
    @FXML
    private VBox taskListVBox, pendingListVBox,completedTaskVBox;


    // Right (detail)
    @FXML
    private CheckBox detailDoneCheck;
    @FXML
    private Label detailMetaLabel, detailTitleLabel;
    @FXML
    private VBox attachmentListVBox;
    @FXML
    private Button addAttachmentBtn;
    //add task
    @FXML
    private CustomDatePicker datePicker = new CustomDatePicker();

    @FXML
    private Button addTaskBtn;
    @FXML
    private TextField quickTaskField;
    @FXML
    private TextArea taskDescription;
    @FXML
    private VBox dateBox;
    @FXML
    private VBox detailPane;
    @FXML
    private VBox courseOption;
    @FXML
    private Label selectedCourseDisplay;

    //filter and selection
    @FXML
    private HBox filterHBox;
    @FXML
    private CustomDatePicker dateFilter = new CustomDatePicker();
    @FXML
    private VBox classListVBox;
    @FXML
    private Button inboxBtn;

    private LocalDate date = LocalDate.now(); //LocalDate.parse("2025-08-10");//LocalDate.now();
    private TaskList regularTasks;
    private TaskList pendingTasks;
    private Task currentDetailTask;
    private MdTextArea detailNote = new MdTextArea();
    private Course selectedCourse = null;

    //lookup table for detail pane
    private final Map<Task, TaskCard> taskCardMap = new HashMap<>();

    // Where each task was before it was moved to "completed" lookup table
    private final Map<Task, VBox> previousContainer = new HashMap<>();


    @FXML
    private void initialize() throws Exception {
        //load data
        UserSettings settings = ConfigManager.load();
        int periodsPerDay = settings.getPeriodsPerDay();
        int daysPerCycle = settings.getDaysPerCycle();
        List<List<String>> courseMatrix = settings.getCourseMatrix();
        List<PeriodTime> periodTimes = settings.getPeriods();

        MasterList userData;
        if (UserDataManager.dataExists()){
            userData = UserDataManager.load();
        } else {
            MasterList newMaster = new MasterList();
            newMaster.addTaskList(new TaskList());
            newMaster.addTaskList(new TaskList());
            UserDataManager.save(newMaster);
            userData = UserDataManager.load();
        }

        regularTasks = userData.getMaster().get(0);
        pendingTasks = userData.getMaster().get(1);

        //load course matrix
        List<List<Course>> schedule = new ArrayList<>();
        for (int i = 0; i < daysPerCycle; i++) {
            List<Course> day = new ArrayList<>();
            for (int j = 0; j < periodsPerDay; j++) {
                day.add(new Course(courseMatrix.get(i).get(j), (char) (65 + i), periodTimes.get(j)));
            }
            schedule.add(day);
        }

        //add the correct courses to display

        setCourseList(date,classListVBox,schedule);



        // Header text
        greetingLabel.setText("Hi, " + settings.getDisplayName());
        dateLabel.setText(humanDate(date));

        detailPane.getChildren().add(detailNote);

        //listen
        detailDoneCheck.selectedProperty().addListener((obs, was, isNow) -> {
            if (currentDetailTask == null) return;

            currentDetailTask.setComplete(isNow);
            TaskCard card = taskCardMap.get(currentDetailTask);
            if (card == null) return;

            card.applyCompletionStyle(isNow);

            Parent parent = card.getParent();
            if (isNow) {
                // store current container to the hashmap, then move to completed
                if (parent instanceof VBox vbox) {
                    previousContainer.put(currentDetailTask, vbox);
                    vbox.getChildren().remove(card);
                }
                completedTaskVBox.getChildren().add(card);
            } else {
                // restore task to where it was
                VBox dest = previousContainer.getOrDefault(currentDetailTask, taskListVBox);
                if (parent instanceof VBox vbox) {
                    vbox.getChildren().remove(card);
                }
                dest.getChildren().add(card);
            }
        });


        //display letter date
        datePicker.setAnnotationProvider(day ->
                new CustomDatePicker.Annotation(String.valueOf(letterDate(day))));
        //dateBox.getChildren().add();
        dateBox.getChildren().add(1,datePicker);

        //listen to date change and display corresponding courses
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            List<Course> avalibleOption = new ArrayList<>();
            selectedCourse = null;//reset selected course
            selectedCourseDisplay.setText("");
            if (letterDate(newValue) != '0') {
                avalibleOption = schedule.get(letterDate2Index(letterDate(newValue)));
            }else {
                avalibleOption.add(new Course("Break", '0', new PeriodTime(0, LocalTime.parse("00:00"), LocalTime.parse("23:59"))));
            }
            courseOption.getChildren().clear(); //clear all the existing display
            for (Course course : avalibleOption) {
                addClassCard(course,courseOption,true);
            }
        });


        //add filter datepicker
        dateFilter.setAnnotationProvider(day ->
                new CustomDatePicker.Annotation(String.valueOf(letterDate(day))));
        filterHBox.getChildren().add(dateFilter);
        //set initial value AKA today
        dateFilter.setValue(date);
        dateFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            //TODO:dynamically change the course display according to the date picked
            setCourseList(newValue,classListVBox,schedule);
        });
        displayInbox();



    }
    public void shutdown() throws Exception {
        System.out.println("closed");
        MasterList userData = new MasterList();
        userData.addTaskList(regularTasks);
        userData.addTaskList(pendingTasks);
        UserDataManager.save(userData);
    }

    @FXML
    public void addTask() {
        String name = quickTaskField.getText();
        String description = taskDescription.getText();
        LocalDate dueDate = datePicker.getValue();

        if (selectedCourse == null) {
            regularTasks.addTask(new Task(name, description, dueDate));
        } else {
            regularTasks.addTask(new Task(name, description, dueDate, selectedCourse));
        }

        // refresh the currently visible view
        refreshVisibleTasks();

        // clear inputs
        quickTaskField.clear();
        taskDescription.clear();
        datePicker.setValue(null);
        selectedCourse = null;
        selectedCourseDisplay.setText("");
    }

    @FXML
    public void displayInbox() {
        taskListVBox.getChildren().clear();
        pendingListVBox.getChildren().clear();
        completedTaskVBox.getChildren().clear();

        subjectTitleLabel.setText("Inbox");
        periodInfoLabel.setText("All your tasks");

        for (Task task : regularTasks.getTaskList()) {
            if (!task.isComplete()) {
                if (task.getDueDate() != null && date.isAfter(task.getDueDate())) {
                    addTaskCard(task, pendingListVBox);
                } else {
                    addTaskCard(task, taskListVBox);
                }
            } else {
                addTaskCard(task, completedTaskVBox);
            }
        }
    }


    /* ========== Public helpers to add dynamic content ========== */

    public void setCenterHeader(String subject, String periodInfo) {
        subjectTitleLabel.setText(subject);
        periodInfoLabel.setText(periodInfo);
    }

    public void setCourseList(LocalDate date,VBox target,List<List<Course>> schedule){
        target.getChildren().clear();
        List<Course> courses = new ArrayList<>();
        if (letterDate(date) != '0') {
            courses = schedule.get(letterDate2Index(letterDate(date)));
        } else {
            courses.add(new Course("Break", '0', new PeriodTime(0, LocalTime.parse("00:00"), LocalTime.parse("23:59"))));
        }

        for (Course course : courses) {
            addClassCard(course, target,false);
        }
    }

    public void addClassCard(Course course, VBox target,boolean isLable) {
        // A class "card" as a Button with title+subtitle (clickable)

        //extract information from the course
        String title = course.getCourseName();
        String subtitle = "Period " + course.getPeriodTime().getPeriodNumber() + " | " + course.getPeriodTime().getStart().toString() + "~" + course.getPeriodTime().getEnd().toString();

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
        if(!isLable){
        card.setOnAction(e -> {
            // change center header when a class is selected
            taskListVBox.getChildren().clear();
            completedTaskVBox.getChildren().clear();

            setCenterHeader(title, subtitle);
            System.out.println("Selected class: " + title);
            for(Task task:regularTasks.getTaskList()){
                if(task.getCourse().getCourseName().equals(title)){
                    if(!task.isComplete()){
                        addTaskCard(task,taskListVBox);
                    } else {
                        addTaskCard(task,completedTaskVBox);
                    }
                }
            }

        });
        }else {
            card.setOnAction(e -> {
                selectedCourse = course;
                selectedCourseDisplay.setText(course.getCourseName());
            });
        }
        target.getChildren().add(card);
    }




    public void addTaskCard(Task task, VBox target) {
        TaskCard card = new TaskCard(task);
        taskCardMap.put(task, card);
        card.setOnOpen(() -> setDetail(task));

        // move to completed save where it came from
        card.setOnMoveToBottom(() -> {
            Parent p = card.getParent();
            if (p instanceof VBox vbox) {
                previousContainer.put(task, vbox);
                vbox.getChildren().remove(card);
            }
            completedTaskVBox.getChildren().add(card);
        });

        // restore from completed to its previous container
        card.setOnRestore(() -> {
            Parent p = card.getParent();
            if (p instanceof VBox vbox) vbox.getChildren().remove(card);
            VBox dest = previousContainer.getOrDefault(task, target);
            dest.getChildren().add(card);
        });

        target.getChildren().add(card);
    }




    private void saveCurrentDetail() {
        if (currentDetailTask != null && detailNote.isDirty()) {
            currentDetailTask.setDescription(detailNote.getText());
            taskCardMap.get(currentDetailTask).setTask(currentDetailTask);
            // TODO:persistant memory goes here
            // StorageManager.saveTask(currentDetailTask);
            detailNote.clearDirty();
        }
    }

    public void setDetail(Task task) {
        saveCurrentDetail();

        this.currentDetailTask = task;
        detailDoneCheck.setSelected(task.isComplete());
        detailMetaLabel.setText(task.getDueDate() != null ? task.getDueDate().toString()+" | "+task.getCourse().getCourseName()+" Period "+task.getCourse().getPeriodTime().getPeriodNumber()+" | "+task.getCourse().getPeriodTime().getStart().toString()+"~"+task.getCourse().getPeriodTime().getEnd().toString(): "");
        detailTitleLabel.setText(task.getName());
        //detailBodyLabel.setText(task.getDescription() != null ? task.getDescription() : "");
        detailNote.setInputSpace(task.getDescription() != null ? task.getDescription() : "");
    }

    private boolean isInboxView() {
        return "Inbox".equals(subjectTitleLabel.getText());
    }

    private void refreshVisibleTasks() {
        if (isInboxView()) {
            displayInbox();
            return;
        }
        String currentCourse = subjectTitleLabel.getText();
        taskListVBox.getChildren().clear();
        completedTaskVBox.getChildren().clear();

        for (Task t : regularTasks.getTaskList()) {
            if (t.getCourse() != null && currentCourse.equals(t.getCourse().getCourseName())) {
                if (!t.isComplete()) {
                    addTaskCard(t, taskListVBox);
                } else {
                    addTaskCard(t, completedTaskVBox);
                }
            }
        }
    }




    /* ========== Internals / stubs ========== */

    private void chooseAttachmentInto(Node containerRow, Label nameLabel) {
        File file = new FileChooser().showOpenDialog(containerRow.getScene().getWindow());
        if (file != null) nameLabel.setText(file.getName());
    }

    private String humanDate(LocalDate d) {
        String dow = d.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String mon = d.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return dow + " " + mon + " " + d.getDayOfMonth() + "," + letterDate(d) + " day";
    }
    //TODO: use better search algorithm

    /**
     * convert localdate to letter day using linear search
     *
     * @param d java LocalDate
     * @return letter date
     */
    private char letterDate(LocalDate d) {
        char letter = '0';
        List<String[]> data = readCSV("test data/letter_day_calendar.csv");
        for (String[] row : data) {
            if (row[0].equals(d.toString())) {
                letter = row[2].charAt(0);
            }
        }
        return letter;
    }

    private int letterDate2Index(char letter) {
        if (letter == '0') {
            return -1;
        } else {
            return (int) letter - 65;
        }
    }

    /**
     * this method reads CSV that maps letter date to actual date
     */
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
