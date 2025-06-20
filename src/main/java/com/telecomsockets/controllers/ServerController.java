package com.telecomsockets.controllers;

import com.telecomsockets.contracts.Controller;
import com.telecomsockets.models.AddressModel;
import com.telecomsockets.models.ChatDictModel;
import com.telecomsockets.models.ChatUser;
import com.telecomsockets.sockets.SocketServer;
import com.telecomsockets.views.ServerView;

import javafx.scene.layout.Region;

public class ServerController extends Controller {

    private final AddressModel addressModel = new AddressModel();
    public final SocketServer server = new SocketServer();
    public final ChatDictModel messages = new ChatDictModel(server.getServerId());

    {
        setTitle("Telecom Sockets Server");

        server.setOnMessageReceived(messages::addMessageToChat);
        server.serverNameProperty().bindBidirectional(addressModel.name);
    }

    public Region getView() {
        return new ServerView(this);
    }

    public AddressModel addressModel() {
        return addressModel;
    }

    @Override
    public void close() throws Exception {
        server.close();
    }

    public void sendMessage(ChatUser receiver, String message) {
        server.sendMessageToReceiver(message, server.toChatUser(), receiver);

    }

}
