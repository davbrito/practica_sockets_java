package com.telecomsockets.views;

import com.telecomsockets.Navigation;
import com.telecomsockets.components.AddressForm;
import com.telecomsockets.components.StatusLabel.StatusData;
import com.telecomsockets.controllers.ClientController;
import com.telecomsockets.models.AddressModel;
import com.telecomsockets.sockets.SocketClient;
import com.telecomsockets.sockets.SocketClient.ConnectionState;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class ClientView extends AppLayoutView {

    private ClientController controller;
    private SocketClient client;

    public ClientView(ClientController controller) {
        super();

        this.controller = controller;
        this.client = controller.client;

        bindTitle("Cliente", controller.addressModel().name);
        this.createView();
    }

    private void createView() {
        setOnBack(this::onBack);
        setOnConnect(e -> onConnect());

        statusDataProperty().bind(StatusData.fromClient(client.connectionStateProperty()));
        subStatusProperty().bind(Bindings.when(client.is(ConnectionState.CONNECTED))
                .then(Bindings.format("server=%s", client.serverNameProperty())).otherwise(""));

        // Solo mostrar si no está conectado
        showConnectProperty().bind(client.is(ConnectionState.CONNECTED).not());
        disableConnectProperty().bind(client.is(ConnectionState.CONNECTING));

        centerProperty().bind(client.is(ConnectionState.CONNECTED)
                .map(connected -> connected ? new ClientConnectedView(controller) : getAddressForm()));

    }

    public void onBack(ActionEvent e) {
        System.out.println("Is connected: " + client.getIsConnected());
        if (client.getIsConnected()) {
            // Si está conectado, desconectar antes de volver
            System.out.println("Desconectando del servidor...");
            client.disconnect();
            return;
        }

        Navigation.toPrimary();
    }

    private void onConnect() {
        // Aquí se manejaría la lógica de conexión al servidor
        System.out.println("Conectando al servidor...");

        AddressModel addressModel = controller.addressModel();
        String ip = addressModel.ip.get();
        int port = addressModel.port.get();
        String name = addressModel.name.get();

        System.out.println(String.format("Intentando conectar a %s:%d", ip, port));

        if (client.getIsConnected()) {
            client.disconnect();
        } else {
            client.connect(name, ip, port);
        }
    }

    private Region getAddressForm() {
        var form = new AddressForm(controller.addressModel());
        form.setOnConnect(e -> onConnect());
        form.formDisabledProperty().bind(client.is(ConnectionState.CONNECTING));
        return form;
    }

    Label errorLabel(String message) {
        var label = new Label(message);
        label.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        return label;
    }
}
