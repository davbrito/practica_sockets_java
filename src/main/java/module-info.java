module com.telecomsockets {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;
    requires javafx.base;

    opens com.telecomsockets to javafx.fxml;
    opens com.telecomsockets.server to javafx.fxml;
    opens com.telecomsockets.client to javafx.fxml;

    exports com.telecomsockets;

}
