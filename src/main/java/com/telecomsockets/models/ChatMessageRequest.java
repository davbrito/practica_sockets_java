package com.telecomsockets.models;

import java.io.Serializable;
import java.util.UUID;

public record ChatMessageRequest(String text, UUID receiverId) implements Serializable {
}
