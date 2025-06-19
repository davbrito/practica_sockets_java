package com.telecomsockets.views;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

public class FormField extends HBox {
    private TextField textField;
    private Label errorLabel;

    public FormField(String labelText, String promptText) {
        super(10);
        Label label = new Label(labelText);
        label.setPrefWidth(180);
        textField = new TextField();
        textField.setPromptText(promptText);

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        var field = new VBox(4, textField, errorLabel);
        HBox.setHgrow(field, Priority.ALWAYS);
        getChildren().addAll(label, field);
    }

    public FormField(String labelText, String promptText, String text) {
        this(labelText, promptText);
        textField.setText(text);
    }

    public FormField(String labelText, String promptText, StringProperty textProperty) {
        this(labelText, promptText);
        textField.textProperty().bindBidirectional(textProperty);
    }

    public FormField(String labelText, String promptText, IntegerProperty integerProperty) {
        this(labelText, promptText);
        textField.textProperty().bindBidirectional(integerProperty, new NumberStringConverter());
    }

    public TextField getTextField() {
        return textField;
    }

    public Label getErrorLabel() {
        return errorLabel;
    }

    public void setError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(!message.isEmpty());
    }

    public StringProperty textProperty() {
        return textField.textProperty();
    }

    public StringProperty errorTextProperty() {
        return errorLabel.textProperty();
    }

}
