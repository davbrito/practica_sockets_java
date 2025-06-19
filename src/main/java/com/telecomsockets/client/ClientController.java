package com.telecomsockets.client;

import java.util.UUID;
import com.telecomsockets.contracts.Controller;
import com.telecomsockets.models.AddressModel;
import com.telecomsockets.models.ChatDictModel;
import com.telecomsockets.models.ChatMessageModel;
import com.telecomsockets.models.ChatUser;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;

public class ClientController extends Controller {

    public final SocketClient client = new SocketClient();
    private final AddressModel _addressModel = new AddressModel();
    private final ChatDictModel messages = new ChatDictModel(client.getClientId());

    {
        setTitle("Telecom Sockets Client");

        client.setOnMessageReceived(messages::addMessageToChat);
    }


    public Region getView() {
        return new ClientView(this);
    }

    public AddressModel addressModel() {
        return _addressModel;
    }

    public BooleanBinding isConnectedProperty() {
        return client.isConnectedProperty();
    }

    public boolean getIsConnected() {
        return client.getIsConnected();
    }

    public BooleanBinding isConnectingProperty() {
        return client.isConnectingProperty();
    }


    public ObjectBinding<ObservableList<ChatMessageModel>> messagesAt(ObservableValue<UUID> id) {
        return messages.messagesAt(id);
    }

    public void addMessageToChat(String text, ChatUser receiver) {
        messages.addMessageToChat(new ChatMessageModel(text, client.toChatUser(), receiver));
    }

    @Override
    public void close() throws Exception {
        client.close();
    }



}
