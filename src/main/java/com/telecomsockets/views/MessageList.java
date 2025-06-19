package com.telecomsockets.views;

import java.util.UUID;
import com.telecomsockets.models.ChatMessageModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MessageList extends ListView<ChatMessageModel> {
    public MessageList(UUID myId) {
        setFocusModel(null);
        setCellFactory(param -> new MessageCell(myId));
        setPrefHeight(200);
        setPlaceholder(new Label("No hay mensajes disponibles"));
    }


    private final class MessageCell extends ListCell<ChatMessageModel> {
        private HBox container;
        private VBox box;

        private Label senderLabel;
        private Label messageLabel;
        private final UUID myId;


        public MessageCell(UUID myId) {
            this.myId = myId;
            senderLabel = new Label();
            senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2a9df4;");

            messageLabel = new Label();
            messageLabel.setWrapText(true);
            messageLabel.setStyle("-fx-text-fill: #000000;");

            box = new VBox(senderLabel, messageLabel);
            box.setPadding(new Insets(8));

            container = new HBox(box);
            container.setPadding(new Insets(4));


        }

        @Override
        protected void updateItem(ChatMessageModel item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                senderLabel.setText(item.sender().name());
                messageLabel.setText(item.text());
                setGraphic(container);

                if (item.sender().id().equals(myId)) {
                    container.setAlignment(Pos.CENTER_RIGHT);
                    box.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 10px 10px 0 10px;");
                } else {
                    container.setAlignment(Pos.CENTER_LEFT);
                    box.setStyle("-fx-background-color: #d0f0c0; -fx-background-radius: 10px 10px 10px 0;");
                }

            }
        }
    }
}
