package com.telecomsockets.client;

import com.telecomsockets.Navigation;
import com.telecomsockets.models.AddressModel;
import com.telecomsockets.views.AddressForm;
import com.telecomsockets.views.AppLayoutView;
import com.telecomsockets.views.StatusLabel.StatusData;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class ClientView extends AppLayoutView {

    private ClientController controller;
    private SocketClient client;
    private AddressModel addressModel;

    public ClientView(ClientController controller) {
        super();

        this.controller = controller;
        this.addressModel = controller.addressModel();
        this.client = controller.client;

        bindTitle("Cliente", controller.addressModel().name);
        this.createView();
    }

    private void createView() {

        setPrefSize(600, 400);
        setOnBack(this::onBack);
        setOnConnect(e -> onConnect());

        statusDataProperty().bind(StatusData.fromClient(client.connectionStateProperty()));
        subStatusProperty().bind(Bindings.when(client.isConnectedProperty())
                .then(Bindings.format("server=%s", client.serverNameProperty())).otherwise(""));

        // Solo mostrar si no está conectado
        showConnectProperty().bind(client.isConnectedProperty().not());
        disableConnectProperty().bind(client.isConnectingProperty());

        centerProperty().bind(client.isConnectedProperty()
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
        var form = new AddressForm(addressModel);
        form.setOnConnect(e -> onConnect());
        form.formDisabledProperty().bind(client.isConnectingProperty());
        return form;
    }

    Label errorLabel(String message) {
        var label = new Label(message);
        label.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        return label;
    }
}
