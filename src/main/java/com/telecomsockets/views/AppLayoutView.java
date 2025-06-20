package com.telecomsockets.views;

import com.telecomsockets.components.StatusLabel;
import com.telecomsockets.components.StatusLabel.StatusData;
import com.telecomsockets.util.RunnableProperty;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class AppLayoutView extends BorderPane {

    private ObjectProperty<StatusData> statusData = new SimpleObjectProperty<>();
    private ObjectProperty<EventHandler<ActionEvent>> onBack = new SimpleObjectProperty<>();
    private RunnableProperty onConnect = new RunnableProperty();
    private BooleanProperty showConnect = new SimpleBooleanProperty(true);
    private BooleanProperty disableConnect = new SimpleBooleanProperty(false);

    public AppLayoutView() {
        super();

        topProperty().set(header());
        bottomProperty().set(footer());
    }

    private StringProperty title = new SimpleStringProperty();

    public StringProperty titleProperty() {
        return title;
    }

    private StringProperty subStatus = new SimpleStringProperty();

    protected StringProperty subStatusProperty() {
        return subStatus;
    }

    public void bindTitle(String title, ReadOnlyStringProperty name) {
        titleProperty()
                .bind(Bindings.when(name.isNull().or(name.isEmpty()))
                        .then(title)
                        .otherwise(Bindings.format("%s (%s)", title, name)));
    }

    private Node header() {
        var titleLabel = new Label();
        titleLabel.textProperty().bind(titleProperty());

        var titlePane = new Pane(titleLabel);
        titlePane.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        var statusLabel = new StatusLabel();
        statusLabel.dataProperty().bind(statusDataProperty());
        statusLabel.subStatusProperty().bind(subStatusProperty());

        var container = new HBox(10, titlePane, statusLabel);
        container.setStyle("-fx-border-color: #ccc; -fx-border-width: 0 0 1 0;");
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(5));
        HBox.setHgrow(titlePane, Priority.ALWAYS);

        return container;
    }

    public ObjectProperty<StatusData> statusDataProperty() {
        return statusData;
    }

    public Region footer() {
        var backButton = new Button("Volver");
        backButton.onActionProperty().bind(onBack);
        backButton.setCancelButton(true);

        var connectButton = new Button("Conectar");
        connectButton.setDefaultButton(true);
        connectButton.onActionProperty().bind(onConnect.map(handler -> e -> {
            if (handler != null) {
                handler.run();
            }
        }));
        // Solo mostrar si no está conectado
        connectButton.visibleProperty().bind(showConnect);
        connectButton.managedProperty().bind(showConnect);
        connectButton.disableProperty().bind(disableConnect);

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        var footer = new HBox(10, backButton, spacer, connectButton);

        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10));
        footer.setStyle("-fx-border-color: #ccc; -fx-border-width: 1 0 0 0; -fx-background-color: #fcfcfc;");

        return footer;
    }

    public BooleanProperty showConnectProperty() {
        return showConnect;
    }

    public BooleanProperty disableConnectProperty() {
        return disableConnect;
    }

    public void setOnBack(EventHandler<ActionEvent> handler) {
        onBack.set(handler);
    }

    public void setOnConnect(Runnable handler) {
        onConnect.set(handler);
    }

}
