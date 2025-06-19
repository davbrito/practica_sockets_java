package com.telecomsockets;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(@SuppressWarnings("exports") Stage s) {
        Navigation.init(this, s);
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Application is stopping...");
        Navigation.cleanUp();
        System.out.println("Application stopped.");
    }

    public static void errorAlert(String title) {
        errorAlert(null, title);
    }

    public static void errorAlert(Throwable e) {
        errorAlert(e, null);
    }

    public static void errorAlert(Throwable e, String title) {
        if (Platform.isFxApplicationThread()) {
            errorAlertImpl(e, title);
        } else {
            Platform.runLater(() -> errorAlertImpl(e, title));
        }
    }

    private static void errorAlertImpl(Throwable e, String title) {
        String message;

        if (title == null || title.isEmpty()) {
            title = "Error";
        }

        if (e != null) {
            e.printStackTrace();
            message = String.format("%s: %s", title, e.getMessage());
        } else {
            message = title;
        }

        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
