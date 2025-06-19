package com.telecomsockets.views;

import com.telecomsockets.controllers.ClientController;
import com.telecomsockets.models.ChatUser;

public class ClientConnectedView extends ChatView {

    private ClientController controller;

    public ClientConnectedView(ClientController controller) {
        super(controller.client.toChatUser());
        this.controller = controller;

        createView();
    }

    private void createView() {
        setItems(controller.client.getUsers());
        messagesProperty().bind(controller.messagesAt(selectedItemProperty().map(ChatUser::id)));
        setOnSendMessage(() -> {
            String message = getInputField().getText().trim();
            if (!message.isEmpty()) {
                var selected = selectedItemProperty().get();
                controller.client.sendMessage(message, selected.id());
                getInputField().clear();
            }
        });
    }

}
