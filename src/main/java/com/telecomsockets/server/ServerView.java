package com.telecomsockets.server;

import com.telecomsockets.MainApp;
import com.telecomsockets.Navigation;
import com.telecomsockets.models.AddressModel;
import com.telecomsockets.server.SocketServer.ServerState;
import com.telecomsockets.views.AddressForm;
import com.telecomsockets.views.AppLayoutView;
import com.telecomsockets.views.StatusLabel.StatusData;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class ServerView extends AppLayoutView {

    private ServerController controller;
    private SocketServer server;

    public ServerView(ServerController controller) {
        super();
        this.controller = controller;
        this.server = controller.server;
        bindTitle("Servidor", controller.addressModel().name);
        createView();
    }

    private void createView() {
        setPrefSize(600, 400);
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
        setOnBack(e -> {
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
        });
        setOnConnect(e -> onConnect());

        statusDataProperty().bind(StatusData.fromServer(server.serverStateProperty()));

        // Solo mostrar si no está conectado
        disableConnectProperty()
                .bind(server.serverStateProperty().isEqualTo(ServerState.LISTENING));

        showConnectProperty()
                .bind(server.serverStateProperty().isNotEqualTo(ServerState.CONNECTED));

    }

    private void onConnect() {
        // Aquí se manejaría la lógica de conexión al servidor

        AddressModel addressModel = controller.addressModel();
        String ip = addressModel.ip.get();
        int port = addressModel.port.get();
        String name = addressModel.name.get();

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
        form.setOnConnect(e -> onConnect());
        form.formDisabledProperty()
                .bind(server.serverStateProperty().isEqualTo(ServerState.LISTENING));
        return form;
    }

    Label errorLabel(String message) {
        var label = new Label(message);
        label.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        return label;
    }
}
