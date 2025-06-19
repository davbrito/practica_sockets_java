package com.telecomsockets.models;

import java.io.Serializable;
import java.util.UUID;

public record ChatUser(UUID id, String name) implements Serializable {

    @Override
    public final String toString() {
        return String.format("ChatUser{id=%s, name='%s'}", id, name);
    }

}
