package com.telecomsockets.views;

import com.telecomsockets.controllers.PrimaryController;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Builder;

public class PrimaryView implements Builder<Region> {

    private PrimaryController controller;

    public PrimaryView(PrimaryController controller) {
        this.controller = controller;
    }

    @Override
    public Region build() {

        HBox hbox = new HBox(20);
        hbox.setPrefSize(320, 200);
        hbox.setAlignment(Pos.CENTER);

        Button clientButton = new Button("Modo cliente");
        clientButton.setOnAction(e -> {
            controller.clientMode();
        });

        Button serverButton = new Button("Modo servidor");
        serverButton.setOnAction(e -> {
            controller.serverMode();
        });

        hbox.getChildren().addAll(clientButton, serverButton);

        return hbox;
    }
}
