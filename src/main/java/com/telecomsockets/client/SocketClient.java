package com.telecomsockets.client;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.function.Consumer;
import com.telecomsockets.MainApp;
import com.telecomsockets.models.ChatMessageModel;
import com.telecomsockets.models.ChatUser;
import com.telecomsockets.services.MessageBrokerService;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class SocketClient {

    public enum ConnectionState {
        CONNECTED, DISCONNECTED, CONNECTING, DISCONNECTING
    }

    private SimpleObjectProperty<ConnectionState> connectionState;
    private StringProperty clientName = new SimpleStringProperty(this, "clientName");
    private Socket clientSocket;
    private UUID clientId = UUID.randomUUID();
    private MessageBrokerService messageBrokerService;

    private void setConnectionState(ConnectionState state) {
        connectionStateProperty().set(state);
    }

    public ConnectionState getConnectionState() {
        return connectionStateProperty().get();
    }

    public boolean getIsConnected() {
        return isConnectedProperty().get();
    }

    public BooleanBinding isConnectedProperty() {
        return connectionStateProperty().isEqualTo(ConnectionState.CONNECTED);
    }

    public BooleanBinding isConnectingProperty() {
        return connectionStateProperty().isEqualTo(ConnectionState.CONNECTING);
    }

    public StringProperty clientNameProperty() {
        return clientName;
    }

    public void setClientName(String name) {
        clientName.set(name);
    }

    public SimpleObjectProperty<ConnectionState> connectionStateProperty() {
        if (connectionState == null) {
            connectionState = new SimpleObjectProperty<>(this, "connectionState", ConnectionState.DISCONNECTED);
        }

        return connectionState;
    }

    private void connectImpl(String ip, int port) throws Exception {
        if (clientSocket != null && clientSocket.isConnected()) {
            MainApp.errorAlert("Ya está conectado al servidor en " + ip + ":" + port);
            return;
        }

        clientSocket = new Socket(ip, port);

        if (!clientSocket.isConnected()) {
            MainApp.errorAlert("No se pudo conectar al servidor en " + ip + ":" + port);
            return;
        }
        messageBrokerService = new MessageBrokerService(clientSocket);


        messageBrokerService.receiver.setOnMessageReceived(this::notifyMessageReceived);
        messageBrokerService.receiver.setOnUsersListReceived(users::setAll);
        messageBrokerService.receiver.setOnHandshake(handshake -> {
            System.out.println("Handshake recibido: " + handshake);
            serverNameProperty().set(handshake.name());
        });

        messageBrokerService.sender.sendHandShake(clientId, getClientName());
        Platform.runLater(() -> setConnectionState(ConnectionState.CONNECTED));
        messageBrokerService.sender.sendRequestClientList();
        System.out.println("Conectado al servidor en " + ip + ":" + port);

        messageBrokerService.receiver.receiveMessages();
    }

    static private String getSocketDisplayName(Socket socket) {
        if (socket == null) {
            return "null";
        }
        var address = socket.getInetAddress();
        return String.format("%s:%d", address.getHostAddress(), socket.getPort());

    }


    public void connect(String name, String ip, int port) {
        setClientName(name);

        var task = new Task<Void>() {
            @Override
            protected void scheduled() {
                setConnectionState(ConnectionState.CONNECTING);
            }

            @Override
            protected Void call() throws Exception {
                connectImpl(ip, port);
                return null;
            }

            @Override
            protected void succeeded() {
                setConnectionState(ConnectionState.DISCONNECTED);
            }

            @Override
            protected void failed() {

                setConnectionState(ConnectionState.DISCONNECTED);
                var error = getException();

                if (error instanceof SocketException && !(error instanceof ConnectException) && clientSocket == null) {
                    return;
                }


                try {
                    SocketClient.this.close();
                } catch (IOException e) {
                }

                if (error instanceof EOFException) {
                    System.out.println("Conexión cerrada por el servidor.");

                    return;
                }

                MainApp.errorAlert(error, "Error");
            }

            @Override
            protected void cancelled() {
                setConnectionState(ConnectionState.DISCONNECTED);
            }
        };

        new Thread(task).start();
    }

    public void disconnect() {
        if (clientSocket == null || !clientSocket.isConnected()) {
            System.out.println("No hay conexión activa para desconectar.");
            return;
        }

        try {
            setConnectionState(ConnectionState.DISCONNECTING);
            close();
            setConnectionState(ConnectionState.DISCONNECTED);
            System.out.println("Desconectado del servidor.");
        } catch (IOException e) {
            if (clientSocket != null && !clientSocket.isClosed()) {
                setConnectionState(ConnectionState.CONNECTED);
            } else {
                setConnectionState(ConnectionState.DISCONNECTED);
            }
            MainApp.errorAlert(e);
        }

    }

    private final ObjectProperty<MessageReceivedHandler> onMessageReceived =
            new SimpleObjectProperty<>(this, "onMessageReceived", null);

    public ObjectProperty<MessageReceivedHandler> onMessageReceivedProperty() {
        return onMessageReceived;
    }

    public void setOnMessageReceived(MessageReceivedHandler callback) {
        onMessageReceived.set(callback);
    }

    private void notifyMessageReceived(ChatMessageModel message) {
        var handler = onMessageReceived.get();
        if (handler != null) {
            handler.accept(message);
        }
    }

    interface MessageReceivedHandler extends Consumer<ChatMessageModel> {
    }


    private ChatUser getUser(UUID userId) {
        return getUsers().stream().filter(user -> user.id().equals(userId)).findFirst().orElse(null);
    }

    public void sendMessage(String message, UUID receiverId) {
        if (clientSocket == null || !clientSocket.isConnected()) {
            MainApp.errorAlert("No hay conexión activa para enviar el mensaje.");
            return;
        }

        notifyMessageReceived(new ChatMessageModel(message, toChatUser(), getUser(receiverId)));
        messageBrokerService.sender.sendMessageToServer(message, clientId, receiverId);
    }

    public String getClientName() {
        if (clientName == null || clientName.get() == null || clientName.get().isEmpty()) {
            return getSocketDisplayName(clientSocket);
        }

        return clientName.get();
    }

    public void close() throws IOException {
        if (clientSocket != null && !clientSocket.isClosed()) {
            clientSocket.close();
        }
        clientSocket = null;
    }

    private StringProperty serverName = new SimpleStringProperty(this, "serverName");

    public StringProperty serverNameProperty() {
        return serverName;
    }

    public String getServerName() {
        return serverName.get();
    }

    private final ObservableList<ChatUser> users = FXCollections.observableArrayList();

    public ObservableList<ChatUser> getUsers() {
        return users;
    }

    public UUID getClientId() {
        return clientId;
    }

    public ChatUser toChatUser() {
        return new ChatUser(clientId, getClientName());
    }

}
