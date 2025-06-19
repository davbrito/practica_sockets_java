package com.telecomsockets.controllers;

import com.telecomsockets.Navigation;
import com.telecomsockets.contracts.Controller;
import com.telecomsockets.views.PrimaryView;
import javafx.scene.layout.Region;

public class PrimaryController extends Controller {
    {
        setTitle("Telecom Sockets");
    }

    public void clientMode() {
        Navigation.toClient();
    }

    public void serverMode() {
        Navigation.toServer();
    }

    public Region getView() {
        return new PrimaryView(this).build();
    }

}
