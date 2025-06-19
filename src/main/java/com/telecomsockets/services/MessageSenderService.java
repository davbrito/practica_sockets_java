package com.telecomsockets.services;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import com.telecomsockets.MainApp;
import com.telecomsockets.models.ChatMessageModel;
import com.telecomsockets.models.ChatMessageRequest;
import com.telecomsockets.models.ChatUser;
import com.telecomsockets.services.MessageReceiverService.GenericRequests;
import com.telecomsockets.services.MessageReceiverService.Handshake;
import com.telecomsockets.services.MessageReceiverService.UserListResponse;

public final class MessageSenderService implements AutoCloseable {
    private ObjectOutputStream out;
    private LinkedBlockingQueue<Serializable> messageQueue;
    private Thread senderThread;
    private static int instanceCount = 0;

    public MessageSenderService(ObjectOutputStream out) {

        if (out == null) {
            throw new IllegalArgumentException("Output stream cannot be null.");
        }

        this.out = out;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.senderThread = new Thread(this::run, "MessageSenderThread-" + instanceCount++);
        this.senderThread.start();
    }

    public void run() {
        try {
            while (true) {
                Serializable data = messageQueue.take();

                try {
                    out.writeObject(data);
                } catch (IOException e) {
                    MainApp.errorAlert(e, "Error al enviar mensaje");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToServer(String text, UUID senderId, UUID receiverId) {
        sendMessageImpl(new ChatMessageRequest(text, senderId, receiverId));
    }

    public void sendMessageToReceiver(String text, ChatUser sender, ChatUser receiver) {
        if (sender == null || receiver == null) {
            throw new IllegalArgumentException("Sender and receiver cannot be null.");
        }
        sendMessageImpl(new ChatMessageModel(text, sender, receiver));
    }

    public void sendMessageToReceiver(ChatMessageModel message) {
        sendMessageImpl(message);
    }

    public void sendHandShake(UUID id, String name) {
        sendMessageImpl(new Handshake(id, name));
    }

    public void sendRequestClientList() {
        sendMessageImpl(GenericRequests.REQUEST_CLIENT_LIST);
    }

    public void sendUserListResponse(List<ChatUser> users) {
        sendMessageImpl(new UserListResponse(users));
    }

    public void close() throws Exception {
        if (out != null) {
            out.close();
        }
        out = null;
        messageQueue.clear();

        if (senderThread != null && senderThread.isAlive()) {
            senderThread.interrupt();
            senderThread.join();
            senderThread = null;
        }

    }

    private void sendMessageImpl(Serializable data) {
        if (data == null) {
            throw new IllegalArgumentException("Data to send cannot be null.");
        }

        if (senderThread.isAlive()) {
            messageQueue.add(data);
        } else {
            throw new IllegalStateException("Sender thread is not running.");
        }

    }

}
