package com.telecomsockets.util;

import javafx.beans.property.SimpleObjectProperty;

public class RunnableProperty extends SimpleObjectProperty<Runnable> implements Runnable {

    public RunnableProperty() {
        super();
    }

    public RunnableProperty(Runnable initialValue) {
        super(initialValue);
    }

    public RunnableProperty(Object bean, String name) {
        super(bean, name);
    }

    public RunnableProperty(Object bean, String name, Runnable initialValue) {
        super(bean, name, initialValue);
    }

    @Override
    public void run() {
        Runnable value = get();

        if (value != null) {
            value.run();
        }
    }
}
