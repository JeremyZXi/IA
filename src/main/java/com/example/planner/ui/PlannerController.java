package com.example.planner.ui;

import com.example.planner.data.StorageManager;
import com.example.planner.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;


import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.scene.input.MouseEvent;
import java.util.function.BiConsumer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    @FXML private Label detailMetaLabel, detailTitleLabel, detailBodyLabel;
    @FXML private VBox attachmentListVBox;
    @FXML private Button addAttachmentBtn;
    //add task
    @FXML private DatePicker picker;
    @FXML private Button addTaskBtn;
    @FXML private TextField quickTaskField;
    @FXML private  TextArea taskDescription;

    private LocalDate date = LocalDate.parse("2025-08-10");//LocalDate.now();
    private TaskList regularTasks;
    private  TaskList pendingTasks;
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
            // Example: change center header when a class is selected
            setCenterHeader(title, subtitle);
            System.out.println("Selected class: " + title);
        });

        classListVBox.getChildren().add(card);
    }

    public void addTodayTaskCard(boolean checked, String title, String timeRange, String preview) {
        HBox row = new HBox(10);
        row.setStyle("-fx-background-color:white; -fx-border-color:#cccccc; -fx-padding:10;");
        row.setMinHeight(72);

        CheckBox cb = new CheckBox();
        cb.setSelected(checked);

        VBox text = new VBox(2);
        Label tt = new Label(title);
        tt.setStyle("-fx-font-weight:bold;");
        Label tr = new Label(timeRange);
        tr.setStyle("-fx-text-fill:#3a6ea5; -fx-font-size:12px;");
        Label pv = new Label(preview);
        pv.setWrapText(true);

        text.getChildren().addAll(tt, tr, pv);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button open = new Button("Open");
        open.setOnAction(e -> setDetail(cb.isSelected(), humanDate(date) + " • " + periodInfoLabel.getText(),
                title, preview + "\n\n(Opened from center list)"));

        row.getChildren().addAll(cb, text, spacer, open);
        taskListVBox.getChildren().add(row);
    }


    public void addPendingTaskCard(Task task) {
        boolean checked = task.isComplete();
        String title = task.getName();
        String meta  = task.getDueDate() != null ? task.getDueDate().toString() : "";
        String preview = task.getDescription() != null ? task.getDescription() : "";

        // Compact preview (first 30 chars only)
        String shortPreview = preview.length() > 30 ? preview.substring(0, 30) + "..." : preview;

        // Content layout
        HBox content = new HBox(8); // smaller spacing
        content.setStyle("-fx-background-color:transparent; -fx-padding:6;");
        content.setMinHeight(48); // smaller height

        CheckBox cb = new CheckBox();
        cb.setSelected(checked);

        VBox text = new VBox(1); // tighter vertical spacing
        Label tt = new Label(title);
        tt.setStyle("-fx-font-weight:bold; -fx-font-size:13px;");
        Label mm = new Label(meta);
        mm.setStyle("-fx-text-fill:#3a6ea5; -fx-font-size:11px;");
        Label pv = new Label(shortPreview);
        pv.setStyle("-fx-text-fill:#555; -fx-font-size:11px;");
        pv.setWrapText(false); // keep it single line
        text.getChildren().addAll(tt, mm, pv);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        content.getChildren().addAll(cb, text, spacer);

        // Button as the card
        Button card = new Button();
        card.setGraphic(content);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(48); // smaller height
        card.setStyle("-fx-background-color:white; -fx-border-color:#cccccc; -fx-alignment:BASELINE_LEFT; -fx-padding:0 6 0 6;");
        HBox.setHgrow(card, Priority.ALWAYS);

        // Clicking the card opens details
        card.setOnAction(e -> setDetail(
                cb.isSelected(),
                meta,
                title,
                preview + "\n\n(Opened from pending)")
        );

        cb.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
            boolean newVal = !cb.isSelected();
            cb.setSelected(newVal);            // show it immediately
            onPendingChecked(task, newVal);    // update the Task object you passed in
            evt.consume();                     // don’t click the card button
        });
        cb.addEventFilter(MouseEvent.MOUSE_RELEASED, MouseEvent::consume);
        cb.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

        // strike-through
        cb.selectedProperty().addListener((o, ov, nv) -> {
            tt.setStyle(nv ? "-fx-font-weight:bold; -fx-strikethrough:true; -fx-font-size:13px;"
                    : "-fx-font-weight:bold; -fx-font-size:13px;");
        });


        pendingListVBox.getChildren().add(card);
    }


    /** Update your model/storage when a pending task checkbox changes. */
    private void onPendingChecked(Task task, boolean checked) {
        task.setComplete(checked);
        // If you want to persist immediately, do it here (adjust to your structures):
        try {
            task.setComplete(checked);
            pendingListVBox.getChildren().remove(task);
        } catch (Exception ex) {
            ex.printStackTrace();
            // optionally show an Alert
        }
    }

    public void setDetail(boolean completed, String meta, String title, String body) {
        detailDoneCheck.setSelected(completed);
        detailMetaLabel.setText(meta);
        detailTitleLabel.setText(title);
        detailBodyLabel.setText(body);
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
