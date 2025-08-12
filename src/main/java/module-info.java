module com.example.planner {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires opencsv;
    requires javafx.markdown.preview.all;
    requires javafx.web;

    opens com.example.planner.ui to javafx.fxml; // for FXML controller reflection
    exports com.example.planner;
    exports com.example.planner.data;
    exports com.example.planner.model;
    exports com.example.planner.ui;


}
