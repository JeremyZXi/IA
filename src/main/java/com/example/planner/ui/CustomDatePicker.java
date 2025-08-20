package com.example.planner.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

/**
 * A DatePicker that can display extra info in each day cell.
 * Supply per-day Annotations (tooltip, small badge text, and/or custom style class).
 */
public class CustomDatePicker extends DatePicker {

    public static class Annotation {
        public final String badgeText;
        public final String tooltip;
        public final String styleClass;

        public Annotation(String badgeText, String tooltip, String styleClass) {
            this.badgeText = badgeText;
            this.tooltip = tooltip;
            this.styleClass = styleClass;
        }

        public Annotation(String badgeText) {
            this.badgeText = badgeText;
            this.tooltip = "";
            this.styleClass = "";
        }

        public static Annotation of(String badgeText, String tooltip) {
            return new Annotation(badgeText, tooltip, null);
        }

        public String getBadge() {
            return this.badgeText;
        }
    }

    private Map<LocalDate, Annotation> annotations = Map.of();

    public CustomDatePicker() {
        super();
        installFactory();
    }

    public void setAnnotations(Map<LocalDate, Annotation> annotations) {
        this.annotations = Objects.requireNonNull(annotations);
        installFactory();
    }


    public void refreshCells() {
        var f = getDayCellFactory();
        setDayCellFactory(null);
        setDayCellFactory(f);
    }

    private java.util.function.Function<LocalDate, Annotation> annotationProvider;

    public void setAnnotationProvider(java.util.function.Function<LocalDate, Annotation> provider) {
        this.annotationProvider = provider;
        installFactory();
    }

    private void installFactory() {
        setDayCellFactory(dp -> new DateCell() {
            private final StackPane badge = buildBadge();
            private final Tooltip tip = new Tooltip();

            {
                setPadding(new Insets(4));
                setGraphicTextGap(4);
            }

            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                setTooltip(null);
                setGraphic(null);
                getStyleClass().removeAll("busy-day", "note-day");

                if (empty || date == null) return;

                // get letter date provider(if avalible)
                Annotation a = null;
                if (annotationProvider != null) {
                    a = annotationProvider.apply(date);
                } else {
                    a = annotations.get(date);
                }

                if (a != null) {
                    if (a.tooltip != null && !a.tooltip.isBlank()) {
                        tip.setText(a.tooltip);
                        setTooltip(tip);
                    }
                    if (a.badgeText != null && !a.badgeText.isBlank()) {
                        ((Label) badge.getChildren().get(0)).setText(a.badgeText);
                        setGraphic(wrapWithCornerBadge(badge));
                    }
                    if (a.styleClass != null && !a.styleClass.isBlank() && !getStyleClass().contains(a.styleClass)) {
                        getStyleClass().add(a.styleClass);
                    }
                }
            }

            private StackPane buildBadge() {
                Label lbl = new Label();
                lbl.setMinSize(18, 18);
                lbl.setPrefSize(18, 18);
                lbl.setMaxSize(18, 18);
                lbl.setStyle("-fx-font-size:10; -fx-font-weight:700; -fx-text-fill:black;");
                return new StackPane(lbl);
            }

            private Node wrapWithCornerBadge(Node b) {
                StackPane wrapper = new StackPane();
                StackPane.setAlignment(b, Pos.TOP_RIGHT);
                StackPane.setMargin(b, new Insets(2, 2, 0, 0));
                wrapper.getChildren().add(b);
                return wrapper;
            }
        });
    }
}
