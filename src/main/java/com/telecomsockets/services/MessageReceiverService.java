package com.telecomsockets.services;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import com.telecomsockets.models.ChatMessageModel;
import com.telecomsockets.models.ChatMessageRequest;
import com.telecomsockets.models.ChatUser;
import javafx.application.Platform;

public class MessageReceiverService {
    private ObjectInputStream in;

    enum GenericRequests {
        REQUEST_CLIENT_LIST
    }

    public MessageReceiverService(ObjectInputStream in) {
        this.in = in;
    }

    public void receiveMessages() throws ClassNotFoundException, IOException {
        try (ObjectInputStream in = this.in) {
            Object temp;

            while ((temp = in.readObject()) != null) {
                if (Thread.interrupted()) {
                    System.out.println("MessageReceiverService thread interrupted, stopping message reception.");
                    break;
                }

                var object = temp;
                Platform.runLater(() -> {
                    switch (object) {
                        case ChatMessageModel message -> notifyMessageReceived(message);
                        case ChatMessageRequest request -> notifyMessageRequest(request);
                        case Handshake handshake -> notifyHandshake(handshake);
                        case UserListResponse response -> notifyUsersListReceived(response.users());
                        case GenericRequests request -> {
                            switch (request) {
                                case REQUEST_CLIENT_LIST -> notifyRequestClientList();
                                default -> System.out.println("Received unknown request: " + request);
                            }
                        }
                        default -> System.out.println("Received unknown object: " + object);
                    };
                });

            }

        }
    }

    private Consumer<ChatMessageModel> onMessageReceived;
    private Consumer<Handshake> onHandshake;
    private Consumer<ChatMessageRequest> onMessageRequest;

    public void setOnMessageReceived(Consumer<ChatMessageModel> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void setOnHandshake(Consumer<Handshake> onHandshake) {
        this.onHandshake = onHandshake;
    }

    public void setOnMessageRequest(Consumer<ChatMessageRequest> onMessageRequest) {
        this.onMessageRequest = onMessageRequest;
    }

    private void notifyMessageReceived(ChatMessageModel message) {
        if (onMessageReceived != null) {
            onMessageReceived.accept(message);
        }
    }

    private void notifyHandshake(Handshake handshake) {
        if (onHandshake != null) {
            onHandshake.accept(handshake);
        }
    }

    private void notifyMessageRequest(ChatMessageRequest request) {
        if (onMessageRequest != null) {
            onMessageRequest.accept(request);
        }
    }

    public record UserListResponse(List<ChatUser> users) implements Serializable {

    }

    public record Handshake(UUID id, String name) implements Serializable {

    }

    Runnable onRequestClientList;

    public void setOnRequestClientList(Runnable onRequestClientList) {
        this.onRequestClientList = onRequestClientList;
    }

    public void notifyRequestClientList() {
        if (onRequestClientList != null) {
            onRequestClientList.run();
        }
    }

    Consumer<List<ChatUser>> onUsersListReceived;

    public void setOnUsersListReceived(Consumer<List<ChatUser>> onUsersListReceived) {
        this.onUsersListReceived = onUsersListReceived;
    }

    public void notifyUsersListReceived(List<ChatUser> users) {
        if (onUsersListReceived != null) {
            onUsersListReceived.accept(users);
        }
    }

    public void close() throws IOException {
        if (in != null) {
            in.close();
            in = null;
        }
    }

}
