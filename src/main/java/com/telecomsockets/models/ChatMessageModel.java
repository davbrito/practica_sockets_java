package com.telecomsockets.models;

import java.io.Serializable;

public record ChatMessageModel(String text, ChatUser sender, ChatUser receiver) implements Serializable {
}
