package com.telecomsockets.primary;

import com.telecomsockets.Navigation;
import com.telecomsockets.contracts.Controller;
import javafx.scene.layout.Region;

public class PrimaryController extends Controller {
    @Override
    public String getTitle() {
        return "Telecom Sockets";
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
