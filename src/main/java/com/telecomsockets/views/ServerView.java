package com.telecomsockets.views;

import com.telecomsockets.MainApp;
import com.telecomsockets.Navigation;
import com.telecomsockets.components.AddressForm;
import com.telecomsockets.components.StatusLabel.StatusData;
import com.telecomsockets.controllers.ServerController;
import com.telecomsockets.models.AddressModel;
import com.telecomsockets.sockets.SocketServer;
import com.telecomsockets.sockets.SocketServer.ServerState;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class ServerView extends AppLayoutView {

    private ServerController controller;
    private SocketServer server;

    public ServerView(ServerController controller) {
        super();

        this.controller = controller;
        this.server = controller.server;

        this.bindTitle("Servidor", server.serverNameProperty());
        this.createView();
    }

    private void createView() {
        setupFooter();
        centerProperty().bind(getContent());
    }

    private ObservableValue<Region> getContent() {
        return server.serverStateProperty().map(state -> switch (state) {
            case STOPPED -> getAddressForm();
            default -> new ServerConnectedView(controller);
        });

    }

    public void setupFooter() {
        setOnBack(this::onBack);

        statusDataProperty().bind(StatusData.fromServer(server.serverStateProperty()));

        // Solo mostrar si no está conectado
        disableConnectProperty().bind(server.is(ServerState.LISTENING));
        showConnectProperty().bind(server.is(ServerState.CONNECTED).not());
    }

    public void onBack(ActionEvent e) {
        if (!server.getIsStopped()) {
            // Si está conectado, desconectar antes de volver
            System.out.println("Desconectando del servidor...");
            try {
                server.stop();
            } catch (Exception e1) {
                MainApp.errorAlert(e1, "Error al desconectar del servidor");
            }
            return;
        }

        Navigation.toPrimary();
    }

    private void onConnect() {
        // Aquí se manejaría la lógica de conexión al servidor

        AddressModel addressModel = controller.addressModel();
        String ip = addressModel.ip.get();
        int port = addressModel.port.get();
        String name = addressModel.name.get();
        System.out.println(addressModel);

        System.out.println(String.format("Iniciando servidor en %s:%d", ip, port));

        try {
            if (!server.getIsStopped()) {
                server.stop();
            } else {
                server.start(name, ip, port);
            }
        } catch (Exception e) {
            MainApp.errorAlert(e, "Error al iniciar el servidor");
        }
    }

    private Region getAddressForm() {
        var form = new AddressForm(controller.addressModel());
        form.setOnConnect(this::onConnect);
        form.formDisabledProperty().bind(server.is(ServerState.LISTENING));
        setOnConnect(form::submit);
        return form;
    }

    Label errorLabel(String message) {
        var label = new Label(message);
        label.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        return label;
    }
}
