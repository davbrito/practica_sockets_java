package com.telecomsockets.views;

import java.util.stream.Stream;
import com.telecomsockets.models.AddressModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class AddressForm extends VBox {
    private FormField portField;
    private FormField addressField;
    private FormField nameField;
    private final AddressModel addressModel;

    private SimpleBooleanProperty formDisabled = new SimpleBooleanProperty(false);

    private ObjectProperty<EventHandler<KeyEvent>> onConnect;

    public AddressForm(AddressModel addressModel) {
        super();
        this.addressModel = addressModel;

        this.setAlignment(Pos.TOP_LEFT);
        this.setPadding(new Insets(10));
        this.setSpacing(10);

        nameField = new FormField("Nombre:", "Ej: Juan", addressModel.name.get());
        addressField = new FormField("Dirección del servidor:", "Ej: 8080", addressModel.ip.get());
        portField = new FormField("Puerto:", "Ej: localhost",
                Integer.toString(addressModel.port.get()));

        Stream.of(nameField, addressField, portField).map(field -> field.getTextField())
                .forEach(field -> {
                    field.disableProperty().bind(formDisabled);
                    field.setOnKeyReleased(this::handleEnterKeyPress);
                });


        portField.getTextField().setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9]*")) {
                return change;
            } else {
                return null; // Reject the change
            }
        }));

        getChildren().addAll(nameField, addressField, portField);

    }

    private void handleEnterKeyPress(KeyEvent e) {
        // enter key pressed, submit
        if (e.getCode() == KeyCode.ENTER && validate()) {
            onConnect.get().handle(e);
        }
    }

    public ObjectProperty<EventHandler<KeyEvent>> onConnectProperty() {
        if (onConnect == null) {
            onConnect = new SimpleObjectProperty<>();
        }
        return onConnect;
    }

    public void setOnConnect(EventHandler<KeyEvent> handler) {
        onConnectProperty().set(handler);
    }

    public boolean validate() {
        String address = addressField.getTextField().getText().trim();
        String port = portField.getTextField().getText().trim();
        String name = nameField.getTextField().getText().trim();

        if (address.isEmpty()) {
            addressField.setError("La dirección no puede estar vacía");
            return false;
        }

        if (port.isEmpty()) {
            portField.setError("El puerto no puede estar vacío");
            return false;
        }

        if (name.isEmpty()) {
            nameField.setError("El nombre no puede estar vacío");
            return false;
        }

        if (!isValidPort(port)) {
            portField.setError("El puerto debe ser un número entre 0 y 65535");
            return false;
        }

        if (!validateIp(address)) {
            addressField.setError("La dirección IP no es válida");
            return false;
        }

        addressField.setError("");
        portField.setError("");
        nameField.setError("");

        addressModel.ip.set(address);
        addressModel.port.set(Integer.parseInt(port));
        addressModel.name.set(name);
        System.out.println("Dirección y puerto validados correctamente.");

        return true;
    }

    public static boolean validateIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        if (ip.equals("localhost")) {
            return true; // Allow localhost as a valid IP
        }
        // Regular expression to validate IPv4 addresses
        String PATTERN =
                "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

        return ip.matches(PATTERN);
    }

    boolean isValidPort(String port) {
        try {
            int p = Integer.parseInt(port);
            return isValidPort(p);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }

    public BooleanProperty formDisabledProperty() {
        return formDisabled;
    }
}
