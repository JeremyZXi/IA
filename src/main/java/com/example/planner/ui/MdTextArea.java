package com.example.planner.ui;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;

import javafx.scene.control.TextArea;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.Objects;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class MdTextArea extends SplitPane {

    @FXML private TextArea inputSpace;
    @FXML private WebView  displaySpace;

    private final BooleanProperty dirty = new SimpleBooleanProperty(false);

    private WebEngine webEngine;
    private Parser parser;
    private HtmlRenderer renderer;
    private String content;

    public MdTextArea() {
        loadFXML();
    }

    private void loadFXML() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MarkdownPane.fxml"));
        loader.setRoot(this);         // fx:root pattern
        loader.setController(this);   // controller is this instance
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load MarkdownPane.fxml", e);
        }
    }


    @FXML
    private void initialize() {
        // init flexmark
        MutableDataSet options = new MutableDataSet();
        parser   = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();

        // init web engine
        webEngine = displaySpace.getEngine();

        // live preview
        inputSpace.textProperty().addListener((obs, oldVal, newVal) -> {
            dirty.set(true);
            content = newVal;
            Node document = parser.parse(newVal == null ? "" : newVal);
            String html = renderer.render(document);
            webEngine.loadContent(wrapWithStyle(html), "text/html");
        });
    }
    private String wrapWithStyle(String bodyHtml) {
        String cssUrl = Objects.requireNonNull(
                getClass().getResource("/css/swiss.css")
        ).toExternalForm();

        return """
        <!doctype html><html><head><meta charset="utf-8">
        <link rel="stylesheet" href="%s">
        </head><body>%s</body></html>
        """.formatted(cssUrl, bodyHtml);
    }


    public void setInputSpace(String inputText) {
        this.inputSpace.setText(inputText);
    }
    public String getText(){
        return content;
    }
    public boolean isDirty() { return dirty.get(); }
    public void clearDirty() { dirty.set(false); }
    public BooleanProperty dirtyProperty() { return dirty; }
}
