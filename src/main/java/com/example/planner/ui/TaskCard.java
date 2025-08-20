package com.example.planner.ui;

import com.example.planner.model.Task;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.io.IOException;
import java.util.function.Consumer;

public class TaskCard extends Button {
    @FXML
    private CheckBox checkBox;
    @FXML
    private Label titleLabel;
    @FXML
    private Label metaLabel;
    @FXML
    private Label previewLabel;

    private final ObjectProperty<Task> task = new SimpleObjectProperty<>();
    private Runnable onOpen;
    private Consumer<Task> onPersist;
    private Runnable onMoveToBottom;
    private Runnable onRestore;

    public TaskCard() {
        loadFXML();
        initBehavior();
    }

    public TaskCard(Task task) {
        this();
        setTask(task);
    }

    private void loadFXML() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TaskCard.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initBehavior() {
        this.setOnAction(e -> {
            if (onOpen != null) onOpen.run();
        });

        checkBox.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
            boolean newVal = !checkBox.isSelected();
            checkBox.setSelected(newVal);

            Task t = getTask();
            if (t != null) t.setComplete(newVal);
            applyCompletionStyle(newVal);
            if (onPersist != null && t != null) onPersist.accept(t);

            if (newVal) {
                if (onMoveToBottom != null) onMoveToBottom.run();
            } else {
                if (onRestore != null) onRestore.run();
            }

            evt.consume();
        });
        checkBox.addEventFilter(MouseEvent.MOUSE_RELEASED, MouseEvent::consume);
        checkBox.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
    }

    public Task getTask() {
        return task.get();
    }

    public void setTask(Task t) {
        task.set(t);
        refreshFromTask();
    }

    public ObjectProperty<Task> taskProperty() {
        return task;
    }

    public void setOnOpen(Runnable onOpen) {
        this.onOpen = onOpen;
    }

    public void setOnRestore(Runnable r) {
        this.onRestore = r;
    }

    public void setOnPersist(Consumer<Task> onPersist) {
        this.onPersist = onPersist;
    }

    public void setOnMoveToBottom(Runnable onMoveToBottom) {
        this.onMoveToBottom = onMoveToBottom;
    }

    private void refreshFromTask() {
        Task t = getTask();
        if (t == null) return;

        titleLabel.setText(nullToEmpty(t.getName()));

        String metaText = "";
        if (t.getDueDate() != null) {
            metaText = t.getDueDate().toString();
            if (t.getCourse() != null) {
                metaText = metaText + " | " + t.getCourse().getCourseName() + " " +
                        "Period " + t.getCourse().getPeriodTime().getPeriodNumber();
            }
            metaLabel.setText(metaText);
        }

        String desc = nullToEmpty(t.getDescription());
        if (desc.length() > 30) {
            previewLabel.setText(desc.substring(0, 30) + "...");
        } else {
            previewLabel.setText(desc);
        }

        checkBox.setSelected(t.isComplete());
        applyCompletionStyle(t.isComplete());
    }

    public void applyCompletionStyle(boolean completed) {
        if (completed) {
            setStyle("-fx-background-color:#f3f3f3; -fx-border-color:#cccccc; -fx-alignment:BASELINE_LEFT; -fx-padding:0 6 0 6;");
            titleLabel.setStyle("-fx-font-weight:bold; -fx-strikethrough:true; -fx-font-size:13px; -fx-text-fill:#666;");
            metaLabel.setStyle("-fx-text-fill:#888; -fx-font-size:11px;");
            previewLabel.setStyle("-fx-text-fill:#888; -fx-font-size:11px;");
            checkBox.setSelected(completed);
            setOpacity(0.85);
            Toolkit.getDefaultToolkit().beep();
        } else {
            setStyle("-fx-background-color:white; -fx-border-color:#cccccc; -fx-alignment:BASELINE_LEFT; -fx-padding:0 6 0 6;");
            titleLabel.setStyle("-fx-font-weight:bold; -fx-font-size:13px;");
            metaLabel.setStyle("-fx-text-fill:#3a6ea5; -fx-font-size:11px;");
            previewLabel.setStyle("-fx-text-fill:#555; -fx-font-size:11px;");
            checkBox.setSelected(completed);
            setOpacity(1.0);
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
