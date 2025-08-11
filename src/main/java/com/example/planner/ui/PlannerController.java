package com.example.planner.ui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class PlannerController {

    // Nav (left narrow)
    @FXML private Button listBtn, matrixBtn, calendarBtn, helpBtn;

    // Left main
    @FXML private Label greetingLabel, dateLabel;
    @FXML private Button addTaskBtn;
    @FXML private TextField quickTaskField;
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

    @FXML
    private void initialize() {
        // Header text
        greetingLabel.setText("Hi, Xiyan");
        dateLabel.setText(humanDate(LocalDate.now()));

        // Hook up buttons (stubs)
        listBtn.setOnAction(e -> System.out.println("List view"));
        matrixBtn.setOnAction(e -> System.out.println("Matrix view"));
        calendarBtn.setOnAction(e -> System.out.println("Calendar view"));
        helpBtn.setOnAction(e -> System.out.println("Help"));

        addTaskBtn.setOnAction(e -> {
            String t = quickTaskField.getText().trim();
            if (!t.isEmpty()) {
                addPendingTaskCard(false, t, "From " + humanDate(LocalDate.now()), "Quick task created from left bar.");
                quickTaskField.clear();
            }
        });

        addAttachmentBtn.setOnAction(e -> chooseAttachment());

        // Demo: a few dynamic items you can remove later
        addClassCard("Chinese Language & Literature", "Period 1 08:00–09:30");
        addClassCard("Math AA HL", "Period 2 09:45–10:15");
        addClassCard("English", "Period 3 10:20–12:15");
        addClassCard("Physics HL", "Period 4 08:00–09:30");

        setCenterHeader("English", "Period 3 10:20–12:15");

        addTodayTaskCard(false, "Anglophone culture presentation", "10:20–10:30",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc maximus.");
        addTodayTaskCard(false, "Anglophone culture presentation", "10:20–10:30",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
        addTodayTaskCard(false, "Anglophone culture presentation", "10:20–10:30",
                "Sapien dui mattis dui, non pulvinar lorem nec erat.");

        addPendingTaskCard(false, "Follow up with Bob", "From Aug 2 2025 • Period 1 • English",
                "Short pending task preview text.");

        setDetail(false, "Aug 11 2025 • Period 3 • English", "Anglophone culture presentation",
                "Select a task to see the full description.\n\nThis pane scrolls for longer content.");
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
        open.setOnAction(e -> setDetail(cb.isSelected(), humanDate(LocalDate.now()) + " • " + periodInfoLabel.getText(),
                title, preview + "\n\n(Opened from center list)"));

        row.getChildren().addAll(cb, text, spacer, open);
        taskListVBox.getChildren().add(row);
    }

    public void addPendingTaskCard(boolean checked, String title, String meta, String preview) {
        HBox row = new HBox(10);
        row.setStyle("-fx-background-color:white; -fx-border-color:#cccccc; -fx-padding:10;");
        row.setMinHeight(72);

        CheckBox cb = new CheckBox();
        cb.setSelected(checked);

        VBox text = new VBox(2);
        Label tt = new Label(title);
        tt.setStyle("-fx-font-weight:bold;");
        Label mm = new Label(meta);
        mm.setStyle("-fx-text-fill:#3a6ea5; -fx-font-size:12px;");
        Label pv = new Label(preview);
        pv.setWrapText(true);

        text.getChildren().addAll(tt, mm, pv);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button open = new Button("Open");
        open.setOnAction(e -> setDetail(cb.isSelected(), meta, title, preview + "\n\n(Opened from pending)"));

        row.getChildren().addAll(cb, text, spacer, open);
        pendingListVBox.getChildren().add(row);
    }

    public void setDetail(boolean completed, String meta, String title, String body) {
        detailDoneCheck.setSelected(completed);
        detailMetaLabel.setText(meta);
        detailTitleLabel.setText(title);
        detailBodyLabel.setText(body);
    }

    public void addAttachment(String fileName) {
        HBox row = new HBox(8);
        Button choose = new Button("Choose file");
        Label name = new Label(fileName);
        name.setWrapText(true);
        row.getChildren().addAll(choose, name);
        choose.setOnAction(e -> chooseAttachmentInto(row, name));
        attachmentListVBox.getChildren().add(row);
    }

    /* ========== Internals / stubs ========== */

    private void chooseAttachment() {
        File file = new FileChooser().showOpenDialog(addAttachmentBtn.getScene().getWindow());
        if (file != null) addAttachment(file.getName());
    }

    private void chooseAttachmentInto(Node containerRow, Label nameLabel) {
        File file = new FileChooser().showOpenDialog(containerRow.getScene().getWindow());
        if (file != null) nameLabel.setText(file.getName());
    }

    private static String humanDate(LocalDate d) {
        String dow = d.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String mon = d.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return dow + " " + mon + " " + d.getDayOfMonth() + " " + d.getYear();
    }
}
