package com.telecomsockets.controllers;

import java.util.UUID;

import com.telecomsockets.contracts.Controller;
import com.telecomsockets.models.AddressModel;
import com.telecomsockets.models.ChatDictModel;
import com.telecomsockets.models.ChatMessageModel;
import com.telecomsockets.sockets.SocketClient;
import com.telecomsockets.views.ClientView;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;

public class ClientController extends Controller {

    public final SocketClient client = new SocketClient();
    private final AddressModel addressModel = new AddressModel();
    private final ChatDictModel messages = new ChatDictModel(client.getClientId());

    {
        setTitle("Telecom Sockets Client");

        client.setOnMessageReceived(messages::addMessageToChat);
        client.clientNameProperty().bindBidirectional(addressModel.name);
    }

    public Region getView() {
        return new ClientView(this);
    }

    public AddressModel addressModel() {
        return addressModel;
    }

    public ObjectBinding<ObservableList<ChatMessageModel>> messagesAt(ObservableValue<UUID> id) {
        return messages.messagesAt(id);
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

}
