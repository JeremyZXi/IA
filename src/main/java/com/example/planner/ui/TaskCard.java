package com.example.planner.ui;

import com.example.planner.model.Task;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.function.Consumer;
public class TaskCard extends Button{
    @FXML private CheckBox checkBox;
    @FXML private Label titleLabel;
    @FXML private Label metaLabel;
    @FXML private Label previewLabel;

    private final ObjectProperty<Task> task = new SimpleObjectProperty<>();
    private Runnable onOpen;               // what to do when card clicked
    private Consumer<Task> onPersist;      // save callback
    private Runnable onMoveToBottom;       // reordering callback

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
        loader.setController(this);     // fx:root pattern
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void initBehavior() {
        // Button click = open
        this.setOnAction(e -> {
            if (onOpen != null) onOpen.run();
        });

        // Prevent checkbox clicks from firing the button action
        checkBox.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
            boolean newVal = !checkBox.isSelected();
            checkBox.setSelected(newVal);
            Task t = getTask();
            if (t != null) t.setComplete(newVal);  // update corresponding Task object
            applyCompletionStyle(newVal);
            if (onPersist != null && t != null) onPersist.accept(t);
            if (newVal && onMoveToBottom != null) onMoveToBottom.run();
            evt.consume();
        });
        checkBox.addEventFilter(MouseEvent.MOUSE_RELEASED, MouseEvent::consume);
        checkBox.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
    }
    public Task getTask() { return task.get(); }
    public void setTask(Task t) {
        task.set(t);
        refreshFromTask();
    }
    public ObjectProperty<Task> taskProperty() { return task; }

    public void setOnOpen(Runnable onOpen) { this.onOpen = onOpen; }
    public void setOnPersist(Consumer<Task> onPersist) { this.onPersist = onPersist; }
    public void setOnMoveToBottom(Runnable onMoveToBottom) { this.onMoveToBottom = onMoveToBottom; }

    // Update labels, checkbox, styles from Task
    private void refreshFromTask() {
        Task t = getTask();
        if (t == null) return;

        titleLabel.setText(nullToEmpty(t.getName()));
        metaLabel.setText(t.getDueDate() != null ? t.getDueDate().toString() : "");
        String desc = nullToEmpty(t.getDescription());
        previewLabel.setText(desc.length() > 30 ? desc.substring(0, 30) + "..." : desc);

        checkBox.setSelected(t.isComplete());
        applyCompletionStyle(t.isComplete());
    }

    public void applyCompletionStyle(boolean completed) {
        if (completed) {
            setStyle("-fx-background-color:#f3f3f3; -fx-border-color:#cccccc; -fx-alignment:BASELINE_LEFT; -fx-padding:0 6 0 6;");
            titleLabel.setStyle("-fx-font-weight:bold; -fx-strikethrough:true; -fx-font-size:13px; -fx-text-fill:#666;");
            metaLabel.setStyle("-fx-text-fill:#888; -fx-font-size:11px;");
            previewLabel.setStyle("-fx-text-fill:#888; -fx-font-size:11px;");
            setOpacity(0.85);
        } else {
            setStyle("-fx-background-color:white; -fx-border-color:#cccccc; -fx-alignment:BASELINE_LEFT; -fx-padding:0 6 0 6;");
            titleLabel.setStyle("-fx-font-weight:bold; -fx-font-size:13px;");
            metaLabel.setStyle("-fx-text-fill:#3a6ea5; -fx-font-size:11px;");
            previewLabel.setStyle("-fx-text-fill:#555; -fx-font-size:11px;");
            setOpacity(1.0);
        }
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}

