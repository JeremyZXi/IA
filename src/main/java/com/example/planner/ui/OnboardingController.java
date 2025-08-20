package com.example.planner.ui;

import com.example.planner.data.ConfigManager;
import com.example.planner.data.StorageManager;
import com.example.planner.model.MasterList;
import com.example.planner.model.PeriodTime;
import com.example.planner.model.UserSettings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OnboardingController {

    // ===== Screens =====
    @FXML
    private AnchorPane screenName;
    @FXML
    private AnchorPane screenSchedule;  // periods/day, days/cycle, and time spinners
    @FXML
    private AnchorPane screenCourses;

    // ===== step 1 =====
    @FXML
    private TextField nameField;
    @FXML
    private Button finishBtn;

    // ===== step 2 =====
    @FXML
    private TextField daysPerCycleField;
    @FXML
    private VBox periodRows; // each row: "Period n" [start]—[end]
    private int periodPerDay = 0;

    // ===== step 3 =====
    @FXML
    private ScrollPane courseScroll;
    @FXML
    private GridPane courseGrid; // rows = periods, cols = days

    // ===== time formatting =====
    private final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private final StringConverter<LocalTime> TIME_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(LocalTime t) {
            return t == null ? "" : TIME_FMT.format(t);
        }

        @Override
        public LocalTime fromString(String s) {
            if (s == null) return null;
            s = s.trim();
            if (s.isEmpty()) return null;
            try {
                return LocalTime.parse(s, TIME_FMT);
            } catch (Exception e) {
                return null;
            }
        }
    };

    @FXML
    private void initialize() {
        // show name first
        show(screenName);
        hide(screenSchedule);
        hide(screenCourses);

        //periodsPerDayField.textProperty().addListener((o, a, b) -> rebuildTimeRows());
        rebuildTimeRows();

        if (finishBtn != null) finishBtn.setOnAction(e -> finish());
    }

    // ===== nav =====
    @FXML
    private void goToSchedule() {
        if (safeTrim(nameField.getText()).isEmpty()) {
            showAlert("Please enter your name.");
            return;
        }
        hide(screenName);
        show(screenSchedule);
        hide(screenCourses);
        if (periodRows.getChildren().isEmpty()) {
            rebuildTimeRows();}
    }

    @FXML
    private void backToName() {
        show(screenName);
        hide(screenSchedule);
        hide(screenCourses);
    }

    @FXML
    private void goToCourses() {
        // validate basic numbers & times first
        //TODO:Optimize prompt text
        Integer periods = periodPerDay;
        Integer days = parsePositiveIntOrNull(daysPerCycleField.getText());
        if (periods == null || days == null || periods <= 0 || days <= 0) {
            showAlert("Please enter valid positive numbers for periods/day and days/cycle.");
            return;
        }

        if (!validateNoOverlapAndExplain()) {
            validateNoOverlapStylesOnly();
            return;
        }

        buildCourseGrid(periods, days);
        hide(screenName);
        hide(screenSchedule);
        show(screenCourses);
    }

    @FXML
    private void backToSchedule() {
        hide(screenName);
        show(screenSchedule);
        hide(screenCourses);
    }
    @FXML
    private void addPeriod(){
        periodPerDay = periodPerDay+1;
        HBox row = new HBox(12);
        row.setFillHeight(true);

        Label lbl = new Label("Period " + (periodPerDay));
        lbl.setMinWidth(90);

        Spinner<LocalTime> start = makeTimeSpinner(LocalTime.of(8, 0));
        Label dash = new Label("—");
        Spinner<LocalTime> end = makeTimeSpinner(LocalTime.of(8, 45));

        // row check
        end.valueProperty().addListener((o, ov, nv) -> {
            LocalTime s = start.getValue();
            if (nv != null && s != null && !nv.isAfter(s)) {
                end.getEditor().setStyle("-fx-background-color:#ffefef;");
            } else end.getEditor().setStyle(null);
        });

        // global overlap, live
        start.valueProperty().addListener((o, ov, nv) -> validateNoOverlapStylesOnly());
        end.valueProperty().addListener((o, ov, nv) -> validateNoOverlapStylesOnly());

        row.getChildren().addAll(lbl, start, dash, end);
        periodRows.getChildren().add(row);
        validateNoOverlapStylesOnly();
    }
    @FXML
    private void deletePeriod(){
        periodPerDay = periodPerDay-1;
        int size = periodRows.getChildren().size();
        if (size > 0) {
            periodRows.getChildren().remove(size - 1);
        }
    }

    private void show(AnchorPane p) {
        p.setVisible(true);
        p.setManaged(true);
    }

    private void hide(AnchorPane p) {
        p.setVisible(false);
        p.setManaged(false);
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    // ===== step 2: time rows =====
    private void rebuildTimeRows() {
        int periods = periodPerDay;
        buildPeriodTimeTemplate(periods);
    }

    private void buildPeriodTimeTemplate(int periodsPerDay) {
        if (periodsPerDay <= 0) return;

        for (int p = 1; p <= periodsPerDay; p++) {
            HBox row = new HBox(12);
            row.setFillHeight(true);

            Label lbl = new Label("Period " + p);
            lbl.setMinWidth(90);

            Spinner<LocalTime> start = makeTimeSpinner(LocalTime.of(8, 0));
            Label dash = new Label("—");
            Spinner<LocalTime> end = makeTimeSpinner(LocalTime.of(8, 45));

            // row check
            end.valueProperty().addListener((o, ov, nv) -> {
                LocalTime s = start.getValue();
                if (nv != null && s != null && !nv.isAfter(s)) {
                    end.getEditor().setStyle("-fx-background-color:#ffefef;");
                } else end.getEditor().setStyle(null);
            });

            // global overlap, live
            start.valueProperty().addListener((o, ov, nv) -> validateNoOverlapStylesOnly());
            end.valueProperty().addListener((o, ov, nv) -> validateNoOverlapStylesOnly());

            row.getChildren().addAll(lbl, start, dash, end);
            periodRows.getChildren().add(row);
        }
        validateNoOverlapStylesOnly();
    }

    private Spinner<LocalTime> makeTimeSpinner(LocalTime initial) {
        Spinner<LocalTime> spinner = new Spinner<>();
        spinner.setEditable(true);
        spinner.setPrefWidth(100);

        SpinnerValueFactory<LocalTime> vf = new SpinnerValueFactory<>() {
            private LocalTime value = initial;

            {
                setConverter(TIME_CONVERTER);
                setValue(value);
            }

            @Override
            public void decrement(int steps) {
                if (value == null) {value = LocalTime.of(0, 0);}
                value = value.minusMinutes(5L * steps);
                setValue(value);
            }

            @Override
            public void increment(int steps) {
                if (value == null) {value = LocalTime.of(0, 0);}
                value = value.plusMinutes(5L * steps);
                setValue(value);
            }
        };
        spinner.setValueFactory(vf);

        spinner.getEditor().focusedProperty().addListener((obs, was, is) -> {
            if (!is) commitEditorText(spinner);
        });
        spinner.getEditor().setOnAction(e -> commitEditorText(spinner));
        return spinner;
    }

    private void commitEditorText(Spinner<LocalTime> spinner) {
        LocalTime parsed = TIME_CONVERTER.fromString(spinner.getEditor().getText());
        if (parsed != null) {
            spinner.getValueFactory().setValue(parsed);
            spinner.getEditor().setStyle(null);
        } else {
            spinner.getEditor().setStyle("-fx-background-color:#ffefef;");
        }
        validateNoOverlapStylesOnly();
    }

    private int parsePositiveInt(String s, int fallback) {
        try {
            int v = Integer.parseInt(s.trim());
            return v > 0 ? v : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private Integer parsePositiveIntOrNull(String s) {
        try {
            int v = Integer.parseInt(s.trim());
            return v > 0 ? v : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ===== overlap helpers =====
    @SuppressWarnings("unchecked")
    private Spinner<LocalTime> getStartSpinner(HBox row) {
        return (Spinner<LocalTime>) row.getChildren().get(1);
    }

    @SuppressWarnings("unchecked")
    private Spinner<LocalTime> getEndSpinner(HBox row) {
        return (Spinner<LocalTime>) row.getChildren().get(3);
    }

    private void validateNoOverlapStylesOnly() {
        for (var n : periodRows.getChildren()) {
            if (n instanceof HBox row) {
                getStartSpinner(row).getEditor().setStyle(null);
                getEndSpinner(row).getEditor().setStyle(null);
            }
        }
        LocalTime lastEnd = null;
        for (var n : periodRows.getChildren()) {
            if (!(n instanceof HBox row)) continue;
            LocalTime s = getStartSpinner(row).getValue();
            LocalTime e = getEndSpinner(row).getValue();
            if (s == null || e == null) continue;

            if (!e.isAfter(s)) {
                getStartSpinner(row).getEditor().setStyle("-fx-background-color:#ffefef;");
                getEndSpinner(row).getEditor().setStyle("-fx-background-color:#ffefef;");
            }
            if (lastEnd != null && s.isBefore(lastEnd)) {
                getStartSpinner(row).getEditor().setStyle("-fx-background-color:#ffefef;");
                int idx = periodRows.getChildren().indexOf(row);
                if (idx > 0 && periodRows.getChildren().get(idx - 1) instanceof HBox prev) {
                    getEndSpinner(prev).getEditor().setStyle("-fx-background-color:#ffefef;");
                }
            }
            if (e != null) lastEnd = (lastEnd == null) ? e : (e.isAfter(lastEnd) ? e : lastEnd);
        }
    }

    private boolean validateNoOverlapAndExplain() {
        LocalTime lastEnd = null;
        int rowIndex = 0;
        for (var n : periodRows.getChildren()) {
            if (!(n instanceof HBox row)) continue;
            rowIndex++;
            LocalTime s = getStartSpinner(row).getValue();
            LocalTime e = getEndSpinner(row).getValue();
            if (s == null || e == null) {
                showAlert("Period " + rowIndex + " has incomplete time settings.");
                return false;
            }
            if (!e.isAfter(s)) {
                showAlert("Period " + rowIndex + " must end after it starts.");
                return false;
            }
            if (lastEnd != null && s.isBefore(lastEnd)) {
                showAlert("Period " + (rowIndex - 1) + " overlaps with Period " + rowIndex +
                        ". Please adjust so the previous period ends before or exactly when the next starts.");
                return false;
            }
            lastEnd = e;
        }
        return true;
    }

    // ===== step 3: courses grid =====
    private void buildCourseGrid(int periodsPerDay, int daysPerCycle) {
        courseGrid.getChildren().clear();
        courseGrid.getColumnConstraints().clear();
        courseGrid.getRowConstraints().clear();

        courseGrid.setHgap(8);
        courseGrid.setVgap(8);
        courseGrid.setPadding(new Insets(8));

        // column constraints (one for row header + N day columns)
        for (int c = 0; c <= daysPerCycle; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(c == 0 ? 18 : (82.0 / daysPerCycle));
            courseGrid.getColumnConstraints().add(cc);
        }
        // row constraints (one for header + M period rows)
        for (int r = 0; r <= periodsPerDay; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(30);
            courseGrid.getRowConstraints().add(rc);
        }

        // headers
        Label corner = new Label("Period / Day");
        corner.setStyle("-fx-font-weight:bold;");
        courseGrid.add(corner, 0, 0);

        for (int d = 1; d <= daysPerCycle; d++) {
            char letter = (char) ('A' + d - 1);
            Label day = new Label("Day " + letter);
            day.setStyle("-fx-font-weight:bold;");
            GridPane.setHalignment(day, HPos.CENTER);
            courseGrid.add(day, d, 0);
        }


        // rows
        for (int p = 1; p <= periodsPerDay; p++) {
            Label rowHead = new Label("Period " + p);
            rowHead.setStyle("-fx-font-weight:bold;");
            courseGrid.add(rowHead, 0, p);

            for (int d = 1; d <= daysPerCycle; d++) {
                TextField tf = new TextField();
                tf.setPromptText("Course");
                // set an id so we can retrieve later if needed
                tf.setId("course_p" + p + "_d" + d);
                courseGrid.add(tf, d, p);
            }
        }
    }

    private List<List<String>> collectCourseMatrix() {
        int periods = periodPerDay;
        int days = Integer.parseInt(daysPerCycleField.getText().trim());
        List<List<String>> matrix = new ArrayList<>(days);

        for (int d = 1; d <= days; d++) {
            List<String> day = new ArrayList<>(periods);
            for (int p = 1; p <= periods; p++) {
                TextField tf = (TextField) lookupNodeInGrid(courseGrid, d, p);
                String name = tf == null ? "" : tf.getText().trim();
                day.add(name);
            }
            matrix.add(day);
        }
        return matrix;
    }

    private static javafx.scene.Node lookupNodeInGrid(GridPane grid, int col, int row) {
        for (javafx.scene.Node n : grid.getChildren()) {
            Integer c = GridPane.getColumnIndex(n);
            Integer r = GridPane.getRowIndex(n);
            if ((c == null ? 0 : c) == col && (r == null ? 0 : r) == row) return n;
        }
        return null;
    }

    // ===== save =====
    @FXML
    public void finish() {
        try {
            String name = safeTrim(nameField.getText());
            if (name.isEmpty()) {
                showAlert("Please enter your name.");
                return;
            }

            int periods = periodPerDay;
            int days = Integer.parseInt(safeTrim(daysPerCycleField.getText()));
            if (periods <= 0 || days <= 0) {
                showAlert("Periods/day and days/cycle must be positive.");
                return;
            }

            // strict overlap validation
            if (!validateNoOverlapAndExplain()) {
                validateNoOverlapStylesOnly();
                return;
            }

            // collect times
            List<PeriodTime> template = new ArrayList<>();
            int periodNumber = 0;
            for (var node : periodRows.getChildren()) {
                if (node instanceof HBox row) {
                    var s = getStartSpinner(row).getValue();
                    var e = getEndSpinner(row).getValue();
                    periodNumber++;
                    template.add(new PeriodTime(periodNumber, s, e));
                }
            }
            if (periodNumber != periods) {
                showAlert("Number of time rows doesn’t match periods per day.");
                return;
            }

            // collect courses (if courses screen was visited, grid exists)
            List<List<String>> courseMatrix = collectCourseMatrix();

            // build settings
            UserSettings settings = new UserSettings();
            settings.setDisplayName(name);
            settings.setDaysPerCycle(days);
            settings.setPeriodsPerDay(periods);
            settings.setPeriods(template);
            settings.getClass().getMethod("setCourseMatrix", List.class).invoke(settings, courseMatrix);
            ConfigManager.save(settings);
            showAlert("Settings saved to:\n" + ConfigManager.settingsPath().toAbsolutePath());

            //build empty to-do list
            //MasterList masterList = new MasterList();
            //StorageManager.save(masterList);

            // TODO: navigate to main UI
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainPlaceholder.fxml"));
            Stage stage = (Stage) screenName.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Planner");
            stage.show();
        } catch (NumberFormatException ex) {
            showAlert("Please enter numbers for periods/day and days/cycle.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Failed to save settings: " + ex.getMessage());
        }
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
