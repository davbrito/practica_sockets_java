package com.telecomsockets.views;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class MessageInputBox extends HBox {
    private TextField textField;
    private ObjectProperty<SendHandler> onSendMessage =
            new SimpleObjectProperty<>(this, "onSendMessage", null);
    {
        textField = new TextField();
        textField.setPromptText("Escribe tu mensaje aquí...");
        textField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                var handler = onSendMessage.get();
                if (handler != null) {
                    handler.handle();
                }
            }
        });
        HBox.setHgrow(textField, Priority.ALWAYS);

        var sendButton = new Button("Enviar");
        sendButton.setOnAction(event -> {
            var handler = onSendMessage.get();
            if (handler != null) {
                handler.handle();
            }
        });

        getChildren().addAll(textField, sendButton);
        setSpacing(10);
    }

    public void setOnSendMessage(SendHandler onSendMessage) {
        this.onSendMessage.set(onSendMessage);
    }

    public String getText() {
        return textField.getText();
    }

    public void clear() {
        textField.clear();
    }

    @FunctionalInterface
    public interface SendHandler {
        void handle();
    }
}
