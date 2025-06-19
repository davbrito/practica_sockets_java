package com.telecomsockets.views;

import java.util.List;
import java.util.UUID;

import com.telecomsockets.controllers.ServerController;
import com.telecomsockets.models.ChatUser;
import com.telecomsockets.sockets.SocketServer.ClientHandler;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public class ServerConnectedView extends ChatView {

    public ServerConnectedView(ServerController controller) {
        super(controller.server.toChatUser());

        var observableMap = controller.server.getClientHandlers();
        var observableList = FXCollections.observableArrayList(mapToList(observableMap));
        observableMap.addListener((MapChangeListener<UUID, ClientHandler>) change -> {
            observableList.setAll(mapToList(change.getMap()));
        });

        setItems(observableList);

        messagesProperty().bind(controller.messages.messagesAt(selectedItemProperty().map(ChatUser::id)));
        setOnSendMessage(() -> {
            String message = getInputField().getText().trim();
            if (!message.isEmpty()) {
                controller.sendMessage(selectedItemProperty().get(), message);
                getInputField().clear();
            }
        });

    }

    private static List<ChatUser> mapToList(ObservableMap<? extends UUID, ? extends ClientHandler> observableMap) {
        return observableMap.values().stream().map(ClientHandler::toChatUser).toList();
    }

}
