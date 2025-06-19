package com.telecomsockets.util;

import java.util.function.Consumer;
import javafx.beans.property.SimpleObjectProperty;

public class ConsumerProperty<T> extends SimpleObjectProperty<Consumer<T>> {

    public ConsumerProperty() {
        super();
    }

    public ConsumerProperty(Consumer<T> initialValue) {
        super(initialValue);
    }

    public ConsumerProperty(Object bean, String name) {
        super(bean, name);
    }

    public ConsumerProperty(Object bean, String name, Consumer<T> initialValue) {
        super(bean, name, initialValue);
    }

    public void accept(T value) {
        Consumer<T> consumer = get();

        if (consumer != null) {
            consumer.accept(value);
        }
    }
}
