package com.telecomsockets.views;

import com.telecomsockets.models.ChatUser;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class ChatUsersListView extends ListView<ChatUser> {
    public ChatUsersListView() {
        super();

        setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ChatUser item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    textProperty().unbind();
                    setText(null);
                    setGraphic(null);
                } else {
                    textProperty().set(item.name());

                }
            }
        });
    }
}
