package com.telecomsockets.services;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MessageBrokerService implements AutoCloseable {
    public final MessageSenderService sender;
    public final MessageReceiverService receiver;


    public MessageBrokerService(Socket socket) throws IOException {
        this.sender = new MessageSenderService(new ObjectOutputStream(socket.getOutputStream()));
        this.receiver = new MessageReceiverService(new ObjectInputStream(socket.getInputStream()));
    }

    @Override
    public void close() throws Exception {
        if (sender != null) {
            sender.close();
        }

        if (receiver != null) {
            receiver.close();
        }
    }

}
