package com.telecomsockets.components;

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
        private Label timestampLabel;
        private final UUID myId;

        public MessageCell(UUID myId) {
            this.myId = myId;
            senderLabel = new Label();
            senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2a9df4;");

            messageLabel = new Label();
            messageLabel.setWrapText(true);
            messageLabel.setStyle("-fx-text-fill: #000000;");

            timestampLabel = new Label();
            timestampLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");

            var timestampBox = new HBox(timestampLabel);
            timestampBox.setAlignment(Pos.CENTER_RIGHT);

            box = new VBox(senderLabel, messageLabel, timestampBox);
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
                boolean isMe = item.sender().id().equals(myId);
                senderLabel.setText(item.sender().name());
                messageLabel.setText(item.text());
                timestampLabel.setText(item.getFormattedSendDate());
                setGraphic(container);

                if (isMe) {
                    container.setAlignment(Pos.CENTER_RIGHT);
                    box.setStyle("-fx-background-color:rgb(215, 231, 240); -fx-background-radius: 10px 10px 0 10px;");
                } else {
                    container.setAlignment(Pos.CENTER_LEFT);
                    box.setStyle("-fx-background-color: #d0f0c0; -fx-background-radius: 10px 10px 10px 0;");
                }

            }
        }
    }
}
