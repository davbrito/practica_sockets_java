package com.telecomsockets;

import java.util.List;
import com.telecomsockets.contracts.Controller;
import com.telecomsockets.controllers.ClientController;
import com.telecomsockets.controllers.PrimaryController;
import com.telecomsockets.controllers.ServerController;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public final class Navigation {

    private static Stage stage;
    private static Controller currentController;

    private Navigation() {}

    private static void setSceneRoot(Parent root) {
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            scene.getStylesheets().add(getStylesheet());
            stage.setScene(scene);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
    }

    public static void init(MainApp app, @SuppressWarnings("exports") Stage s) {
        List<String> rawParameters = app.getParameters().getRaw();
        String mode = rawParameters.size() > 0 ? rawParameters.get(0) : "primary";
        stage = s;

        // Set the initial root view
        switch (mode.toLowerCase()) {
            case "client":
                toClient();
                break;
            case "server":
                toServer();
                break;
            default:
                toPrimary();
                break;
        }
    }

    public static void toPrimary() {
        setRoot(PrimaryController.class);
    }

    public static void toClient() {
        setRoot(ClientController.class);
    }

    public static void toServer() {
        setRoot(ServerController.class);
    }

    private static <T extends Controller> void setRoot(Class<T> controllerClass) {
        try {
            T controller = controllerClass.getDeclaredConstructor().newInstance();
            changeController(controller);
            setRoot(controller);

        } catch (Exception e) {
            MainApp.errorAlert(e, String.format("Error initializing controller (%s)", controllerClass.getSimpleName()));
        }
    }

    private static void setRoot(Controller controller) {
        Region root = controller.getView();
        root.setPrefSize(600, 400);
        setSceneRoot(root);
        stage.titleProperty().bind(controller.titleProperty().orElse("Telecom Sockets"));
        stage.show();
    }

    private static String getStylesheet() {
        return MainApp.class.getResource("/styles/Styles.css").toExternalForm();
    }

    private static void changeController(Controller newController) throws Exception {
        if (currentController != null) {
            currentController.close();
        }
        currentController = newController;
    }

    static void cleanUp() throws Exception {
        changeController(null);
    }
}
