package com.telecomsockets.views;

import com.telecomsockets.client.SocketClient.ConnectionState;
import com.telecomsockets.server.SocketServer.ServerState;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class StatusLabel extends HBox {

    private ObjectProperty<StatusData> data = new SimpleObjectProperty<>();

    public StatusLabel(String text, String color) {
        this(new StatusData(text, color));
    }

    public StatusLabel(StatusData statusData) {
        this();
        data.set(statusData);
    }


    public StatusLabel() {
        super();
        getStyleClass().add("status-label");
        setSpacing(4);

        var label = new Label();
        label.getStyleClass().add("status-label-text");
        label.textProperty().bind(data.map(StatusData::message));

        var subLabel = new Label();
        subLabel.getStyleClass().add("status-label-sub");
        subLabel.textProperty().bind(subStatusProperty());
        var hasSubStatus = subStatusProperty().map(s -> s != null && !s.isEmpty());
        subLabel.visibleProperty().bind(hasSubStatus);
        subLabel.managedProperty().bind(hasSubStatus);

        getChildren().addAll(label, subLabel);



        // textProperty().bind(data.map(StatusData::message));
        styleProperty().bind(data.map(x -> {
            var color = Color.web(x.color);
            return String.format("-fx-background-color: linear-gradient(to bottom right, %s, %s)",
                    toHex(color.brighter()), toHex(color.darker()));

            // return String.format("-fx-background-color: %s; ", x.color);
        }));
    }

    String toHex(Color color) {
        return String.format("rgb(%d, %d, %d)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }


    public ObjectProperty<StatusData> dataProperty() {
        return data;
    }

    public record StatusData(String message, String color) {

        public static StatusData of(ConnectionState connectionState) {
            return switch (connectionState) {
                case CONNECTED -> new StatusData("Conectado", "rgb(13, 131, 86)"); // Verde
                case DISCONNECTED -> new StatusData("Desconectado", "rgb(255, 0, 0)"); // Rojo
                case CONNECTING -> new StatusData("Conectando...", "rgb(255, 165, 0)"); // Naranja
                case DISCONNECTING -> new StatusData("Desconectando...", "rgb(255, 165, 0)"); // Naranja
            };
        }

        public static StatusData of(ServerState serverState) {
            return switch (serverState) {
                case CONNECTED -> new StatusData("Servidor conectado", "rgb(13, 131, 86)"); // Verde
                case LISTENING -> new StatusData("Servidor escuchando...", "rgb(13, 131, 86)"); // Verde
                case STOPPED -> new StatusData("Servidor detenido", "rgb(255, 0, 0)"); // Rojo
            };
        }

        public static ObservableValue<StatusData> fromServer(ObjectProperty<ServerState> state) {
            return state.map(StatusData::of);
        }

        public static ObservableValue<StatusData> fromClient(ObjectProperty<ConnectionState> state) {
            return state.map(StatusData::of);
        }
    }

    private StringProperty subStatus = new SimpleStringProperty();

    public StringProperty subStatusProperty() {
        return subStatus;
    }

}
