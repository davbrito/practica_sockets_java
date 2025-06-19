package com.telecomsockets.models;

import java.util.UUID;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class ChatDictModel {
    private final ObservableMap<UUID, ObservableList<ChatMessageModel>> clientMessages =
            FXCollections.observableHashMap();

    public ObjectBinding<ObservableList<ChatMessageModel>> messagesAt(ObservableValue<UUID> id) {
        return Bindings.valueAt(clientMessages, id);
    }

    private final UUID selfId;

    public ChatDictModel(UUID selfId) {
        this.selfId = selfId;
    }


    public void addMessageToChat(ChatMessageModel message) {
        if (!message.sender().id().equals(selfId) && !message.receiver().id().equals(selfId)) {
            System.out.println("Ignoring message not sent to or from self: " + message);
            return;
        }

        clientMessages.computeIfAbsent(getKeyForMessage(message), k -> FXCollections.observableArrayList())
                .add(message);
    }

    private UUID getKeyForMessage(ChatMessageModel message) {
        if (message.sender().id().equals(selfId)) {
            return message.receiver().id();
        } else if (message.receiver().id().equals(selfId)) {
            return message.sender().id();
        }

        return null; // Should not happen if the model is used correctly
    }
}
