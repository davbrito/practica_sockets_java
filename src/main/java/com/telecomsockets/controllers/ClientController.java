package com.telecomsockets.controllers;

import com.telecomsockets.contracts.Controller;
import com.telecomsockets.models.AddressModel;
import com.telecomsockets.sockets.SocketClient;
import com.telecomsockets.views.ClientView;
import javafx.scene.layout.Region;

public class ClientController extends Controller {

    public final SocketClient client = new SocketClient();
    private final AddressModel addressModel = new AddressModel();

    {
        setTitle("Telecom Sockets Client");

        client.clientNameProperty().bindBidirectional(addressModel.name);
    }

    public Region getView() {
        return new ClientView(this);
    }

    public AddressModel addressModel() {
        return addressModel;
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

}
