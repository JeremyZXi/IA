package com.example.planner.ui;

import com.example.planner.data.ConfigManager;
import com.example.planner.model.PeriodTime;
import com.example.planner.model.UserSettings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OnboardingController {

    // === root panes (two steps in one FXML) ===
    @FXML private AnchorPane screenName;
    @FXML private AnchorPane screenSchedule;

    // === step 1 ===
    @FXML private TextField nameField;
    @FXML private Button    finishBtn; // optional: if you gave it an fx:id

    // === step 2 ===
    @FXML private TextField periodsPerDayField;
    @FXML private TextField daysPerCycleField;
    @FXML private VBox      periodRows; // container inside your ScrollPane

    // Time formatting for spinners
    private final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private final StringConverter<LocalTime> TIME_CONVERTER = new StringConverter<>() {
        @Override public String toString(LocalTime t) { return t == null ? "" : TIME_FMT.format(t); }
        @Override public LocalTime fromString(String s) {
            if (s == null) return null;
            s = s.trim();
            if (s.isEmpty()) return null;
            try { return LocalTime.parse(s, TIME_FMT); } catch (Exception e) { return null; }
        }
    };

    @FXML
    private void initialize() {
        // show name screen first
        setPane(screenName, true);
        setPane(screenSchedule, false);

        // rebuild period rows when "periods per day" changes
        periodsPerDayField.textProperty().addListener((o, a, b) -> rebuildRows());

        // first-time build (handles empty input gracefully)
        rebuildRows();

        // safety: if Scene Builder dropped the onAction, wire the finish button here
        if (finishBtn != null) finishBtn.setOnAction(e -> finish());
    }

    // === navigation ===
    @FXML
    private void goToSchedule() {
        if (safeTrim(nameField.getText()).isEmpty()) {
            showAlert("Please enter your name.");
            return;
        }
        setPane(screenName, false);
        setPane(screenSchedule, true);
        if (periodRows.getChildren().isEmpty()) rebuildRows();
    }

    @FXML
    private void backToName() {
        setPane(screenSchedule, false);
        setPane(screenName, true);
    }

    private void setPane(AnchorPane pane, boolean show) {
        pane.setVisible(show);
        pane.setManaged(show);
    }

    private String safeTrim(String s) { return s == null ? "" : s.trim(); }

    // === UI building ===
    private void rebuildRows() {
        int periods = parsePositiveInt(periodsPerDayField.getText(), 0);
        buildPeriodTimeTemplate(periods);
    }

    /** Build one set of period-time rows; these times apply to every day. */
    private void buildPeriodTimeTemplate(int periodsPerDay) {
        periodRows.getChildren().clear();
        if (periodsPerDay <= 0) return;

        for (int p = 1; p <= periodsPerDay; p++) {
            HBox row = new HBox(12);
            row.setFillHeight(true);

            Label lbl = new Label("Period " + p);
            lbl.setMinWidth(90);

            Spinner<LocalTime> start = makeTimeSpinner(LocalTime.of(8, 0));   // default 08:00
            Label dash = new Label("—");
            Spinner<LocalTime> end   = makeTimeSpinner(LocalTime.of(8, 45));  // default 08:45

            // simple visual validation: end must be after start
            end.valueProperty().addListener((o, ov, nv) -> {
                LocalTime s = start.getValue();
                if (nv != null && s != null && !nv.isAfter(s)) {
                    end.getEditor().setStyle("-fx-background-color: #ffefef;");
                } else {
                    end.getEditor().setStyle(null);
                }
            });

            row.getChildren().addAll(lbl, start, dash, end);
            periodRows.getChildren().add(row);
        }
    }

    private Spinner<LocalTime> makeTimeSpinner(LocalTime initial) {
        Spinner<LocalTime> spinner = new Spinner<>();
        spinner.setEditable(true);
        spinner.setPrefWidth(100);

        SpinnerValueFactory<LocalTime> vf = new SpinnerValueFactory<>() {
            private LocalTime value = initial;
            { setConverter(TIME_CONVERTER); setValue(value); }

            @Override public void decrement(int steps) {
                if (value == null) value = LocalTime.of(0, 0);
                value = value.minusMinutes(5L * steps);
                setValue(value);
            }
            @Override public void increment(int steps) {
                if (value == null) value = LocalTime.of(0, 0);
                value = value.plusMinutes(5L * steps);
                setValue(value);
            }
        };
        spinner.setValueFactory(vf);

        // commit typed text on focus loss / Enter
        spinner.getEditor().focusedProperty().addListener((obs, was, is) -> {
            if (!is) commitEditorText(spinner);
        });
        spinner.getEditor().setOnAction(e -> commitEditorText(spinner));

        return spinner;
    }

    private void commitEditorText(Spinner<LocalTime> spinner) {
        String text = spinner.getEditor().getText();
        LocalTime parsed = TIME_CONVERTER.fromString(text);
        if (parsed != null) {
            spinner.getValueFactory().setValue(parsed);
            spinner.getEditor().setStyle(null);
        } else {
            spinner.getEditor().setStyle("-fx-background-color: #ffefef;");
        }
    }

    private int parsePositiveInt(String s, int fallback) {
        try {
            int v = Integer.parseInt(s.trim());
            return v > 0 ? v : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    // === save ===
    @FXML
    public void finish() {
        try {
            String name = safeTrim(nameField.getText());
            if (name.isEmpty()) {
                showAlert("Please enter your name.");
                return;
            }

            int periods = Integer.parseInt(safeTrim(periodsPerDayField.getText()));
            int days    = Integer.parseInt(safeTrim(daysPerCycleField.getText()));
            if (periods <= 0 || days <= 0) {
                showAlert("Periods/day and days/cycle must be positive.");
                return;
            }

            // Collect the daily template from spinners
            List<PeriodTime> template = new ArrayList<>();
            int periodNumber = 0;

            for (var node : periodRows.getChildren()) {
                if (node instanceof HBox row) {
                    var kids = row.getChildren();
                    if (kids.size() >= 4
                            && kids.get(1) instanceof Spinner
                            && kids.get(3) instanceof Spinner) {

                        @SuppressWarnings("unchecked")
                        Spinner<LocalTime> sSpin = (Spinner<LocalTime>) kids.get(1);
                        @SuppressWarnings("unchecked")
                        Spinner<LocalTime> eSpin = (Spinner<LocalTime>) kids.get(3);
                        LocalTime s = sSpin.getValue();
                        LocalTime e = eSpin.getValue();

                        if (s == null || e == null || !e.isAfter(s)) {
                            showAlert("Please set valid times (end > start) for all periods.");
                            return;
                        }

                        periodNumber++;
                        template.add(new PeriodTime(periodNumber, s, e));
                    }
                }
            }

            if (periodNumber != periods) {
                showAlert("Number of time rows doesn’t match periods per day.");
                return;
            }

            // Build UserSettings and save via your ConfigManager
            UserSettings settings = new UserSettings();
            settings.setDisplayName(name);
            settings.setDaysPerCycle(days);
            settings.setPeriodsPerDay(periods);
            settings.setPeriods(template);

            ConfigManager.save(settings);

            showAlert("Settings saved to:\n" + ConfigManager.settingsPath().toAbsolutePath());

            // TODO: Navigate to your main UI
            // Navigator.loadMainPlaceholder();

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
