package com.example.planner.ui;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TimePicker extends VBox {

    // FXML
    @FXML private Spinner<Integer> startHour;
    @FXML private Spinner<Integer> startMinute;
    @FXML private Spinner<Integer> endHour;
    @FXML private Spinner<Integer> endMinute;
    @FXML private Button nowBtn, plus30Btn, plus1hBtn, clearBtn, okBtn, cancelBtn;
    @FXML private Label  durationLabel;

    // Properties (对外暴露 API)
    private final ObjectProperty<LocalTime> startTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> endTime   = new SimpleObjectProperty<>();
    private final ReadOnlyObjectWrapper<Duration> duration = new ReadOnlyObjectWrapper<>();

    // Callbacks
    private Runnable onApply;
    private Runnable onCancel;

    private final DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");

    public TimePicker() {
        loadFXML();
        initBehavior();
    }

    /* ===== lifecycle ===== */

    private void loadFXML() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TimeRangePicker.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load TimeRangePicker.fxml", e);
        }
    }

    private void initBehavior() {
        initSpinner(startHour, 0, 23);
        initSpinner(startMinute, 0, 59);
        initSpinner(endHour, 0, 23);
        initSpinner(endMinute, 0, 59);

        // 默认值
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        setStartTime(now);
        setEndTime(now.plusHours(1));

        // UI -> 属性
        Runnable pushUIToProps = () -> {
            LocalTime s = LocalTime.of(startHour.getValue(), startMinute.getValue());
            LocalTime e = LocalTime.of(endHour.getValue(), endMinute.getValue());
            startTime.set(s);
            endTime.set(e);
            duration.set(computeDuration(s, e));
            updateDurationLabel();
            applyValidStyle(true); // 你也可以在这里做更多校验后切换样式
        };

        startHour.valueProperty().addListener((o, a, b) -> pushUIToProps.run());
        startMinute.valueProperty().addListener((o, a, b) -> pushUIToProps.run());
        endHour.valueProperty().addListener((o, a, b) -> pushUIToProps.run());
        endMinute.valueProperty().addListener((o, a, b) -> pushUIToProps.run());

        // 属性 -> UI（当外部 setStartTime / setEndTime）
        startTime.addListener((o, a, s) -> {
            if (s != null) {
                startHour.getValueFactory().setValue(s.getHour());
                startMinute.getValueFactory().setValue(s.getMinute());
            }
            duration.set(computeDuration(getStartTime(), getEndTime()));
            updateDurationLabel();
        });
        endTime.addListener((o, a, e) -> {
            if (e != null) {
                endHour.getValueFactory().setValue(e.getHour());
                endMinute.getValueFactory().setValue(e.getMinute());
            }
            duration.set(computeDuration(getStartTime(), getEndTime()));
            updateDurationLabel();
        });

        // 快捷键
        nowBtn.setOnAction(e -> {
            LocalTime t = LocalTime.now().withSecond(0).withNano(0);
            setStartTime(t);
            setEndTime(t.plusHours(1));
        });
        plus30Btn.setOnAction(e -> setEndTime(getStartTime().plusMinutes(30)));
        plus1hBtn.setOnAction(e -> setEndTime(getStartTime().plusHours(1)));
        clearBtn.setOnAction(e -> {
            setStartTime(LocalTime.of(0, 0));
            setEndTime(LocalTime.of(0, 0));
        });

        // 行为回调
        okBtn.setOnAction(e -> {
            if (onApply != null) onApply.run();
        });
        cancelBtn.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
        });

        // 初始一次
        updateDurationLabel();
    }

    /* ===== 样式/展示 ===== */

    private void updateDurationLabel() {
        Duration d = getDuration();
        long mins = d.toMinutes();
        long h = mins / 60;
        long m = mins % 60;
        durationLabel.setText("%s – %s  (%dh %02dm)".formatted(
                tf.format(getStartTime()), tf.format(getEndTime()), h, m));
    }

    /** 可根据校验切换样式（示例：简单高亮）*/
    public void applyValidStyle(boolean valid) {
        if (valid) {
            setStyle("-fx-background-color:transparent;");
            durationLabel.setStyle("-fx-text-fill:-fx-text-base-color;");
        } else {
            setStyle("-fx-background-color:#fff5f5; -fx-border-color:#ffcccc;");
            durationLabel.setStyle("-fx-text-fill:#d14343;");
        }
    }

    /* ===== Spinner 初始化与编辑提交 ===== */

    private void initSpinner(Spinner<Integer> sp, int min, int max) {
        sp.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max));
        sp.setEditable(true);
        sp.getEditor().setTextFormatter(integerTextFormatter(min, max));

        // 失焦提交
        sp.focusedProperty().addListener((obs, was, isNow) -> {
            if (!isNow) commitEditorText(sp);
        });
    }

    private TextFormatter<Integer> integerTextFormatter(int min, int max) {
        return new TextFormatter<>(new StringConverter<>() {
            @Override public String toString(Integer value) {
                return value == null ? "" : String.format("%02d", value);
            }
            @Override public Integer fromString(String s) {
                if (s == null || s.isBlank()) return min;
                try {
                    int v = Integer.parseInt(s.trim());
                    if (v < min) v = min;
                    if (v > max) v = max;
                    return v;
                } catch (NumberFormatException e) {
                    return min;
                }
            }
        });
    }

    private void commitEditorText(Spinner<Integer> spinner) {
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<Integer> vf = spinner.getValueFactory();
        if (vf != null) {
            @SuppressWarnings("unchecked")
            TextFormatter<Integer> tfm = (TextFormatter<Integer>) spinner.getEditor().getTextFormatter();
            Integer val = tfm.getValueConverter().fromString(text);
            vf.setValue(val);
        }
    }

    /* ===== 公共 API（与 TaskCard 风格一致）===== */

    public LocalTime getStartTime() { return startTime.get(); }
    public void setStartTime(LocalTime t) {
        Objects.requireNonNull(t);
        startTime.set(t.withSecond(0).withNano(0));
    }
    public ObjectProperty<LocalTime> startTimeProperty() { return startTime; }

    public LocalTime getEndTime() { return endTime.get(); }
    public void setEndTime(LocalTime t) {
        Objects.requireNonNull(t);
        endTime.set(t.withSecond(0).withNano(0));
    }
    public ObjectProperty<LocalTime> endTimeProperty() { return endTime; }

    public Duration getDuration() { return duration.get(); }
    public ReadOnlyObjectProperty<Duration> durationProperty() { return duration.getReadOnlyProperty(); }

    public void setOnApply(Runnable onApply) { this.onApply = onApply; }
    public void setOnCancel(Runnable onCancel) { this.onCancel = onCancel; }

    /* ===== 业务逻辑 ===== */

    /** 允许跨天：若 end < start，则视为跨越午夜到次日 */
    private Duration computeDuration(LocalTime s, LocalTime e) {
        if (s == null || e == null) return Duration.ZERO;
        int sSec = s.toSecondOfDay();
        int eSec = e.toSecondOfDay();
        int delta = eSec - sSec;
        if (delta < 0) delta += 24 * 60 * 60;
        return Duration.ofSeconds(delta);
    }
}
