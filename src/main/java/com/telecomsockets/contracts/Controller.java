package com.telecomsockets.contracts;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Region;

public abstract class Controller implements AutoCloseable {
    private StringProperty title;

    public StringProperty titleProperty() {
        if (title == null) {
            title = new SimpleStringProperty(this, "title");
        }
        return title;
    }

    public String getTitle() {
        return titleProperty().get();
    }

    public void setTitle(String title) {
        titleProperty().set(title);
    }

    abstract public Region getView();

    /**
     * Closes the controller and releases any resources it holds. This method should be overridden by
     * subclasses to perform specific cleanup tasks, such as closing network connections, stopping
     * threads, or releasing UI components.
     */
    @Override
    public void close() throws Exception {}
}
