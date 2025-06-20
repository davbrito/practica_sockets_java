package com.telecomsockets.sockets;

import java.io.EOFException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.function.Consumer;
import com.telecomsockets.MainApp;
import com.telecomsockets.models.ChatMessageModel;
import com.telecomsockets.models.ChatUser;
import com.telecomsockets.services.MessageBrokerService;
import com.telecomsockets.util.ConsumerProperty;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SocketClient {

    public enum ConnectionState {
        CONNECTED, DISCONNECTED, CONNECTING, DISCONNECTING
    }

    private SimpleObjectProperty<ConnectionState> connectionState;
    private StringProperty clientName = new SimpleStringProperty(this, "clientName");
    private Socket clientSocket;
    private UUID clientId = UUID.randomUUID();
    private MessageBrokerService messageBrokerService;
    private Thread backgroundThread;

    public SocketClient() {}


    private void connect(String ip, int port) throws Exception {
        if (clientSocket != null && clientSocket.isConnected()) {
            MainApp.errorAlert("Ya está conectado al servidor en " + ip + ":" + port);
            return;
        }

        clientSocket = new Socket(ip, port);

        if (!clientSocket.isConnected()) {
            MainApp.errorAlert("No se pudo conectar al servidor en " + ip + ":" + port);
            return;
        }
        messageBrokerService = new MessageBrokerService(clientSocket, clientId);

        messageBrokerService.receiver.setOnMessageReceived(onMessageReceived::accept);
        messageBrokerService.receiver.setOnUsersListReceived(users::setAll);
        messageBrokerService.receiver.setOnHandshake(handshake -> {
            System.out.println("Handshake recibido: " + handshake);
            serverNameProperty().set(handshake.name());
        });

        messageBrokerService.sender.sendHandShake(clientId, getClientName());

        setConnectionState(ConnectionState.CONNECTED);

        messageBrokerService.sender.sendRequestClientList();
        System.out.println("Conectado al servidor en " + ip + ":" + port);

        messageBrokerService.receiver.receiveMessages();
    }

    public void start(String ip, int port) {
        if (backgroundThread != null && backgroundThread.isAlive()) {
            backgroundThread.interrupt();
        }

        backgroundThread = new Thread(runClient(ip, port), "SocketClient-BackgroundThread");
        backgroundThread.start();
    }

    private Runnable runClient(String ip, int port) {
        return () -> {
            try {
                setConnectionState(ConnectionState.CONNECTING);
                connect(ip, port);
                setConnectionState(ConnectionState.DISCONNECTED);
            } catch (final Exception error) {
                handlerError(error);
            }
        };
    }



    private void handlerError(Exception error) {
        setConnectionState(ConnectionState.DISCONNECTED);

        if (error instanceof SocketException && !(error instanceof ConnectException) && clientSocket == null) {
            return;
        }

        try {
            SocketClient.this.close();
        } catch (Exception e) {
        }

        if (error instanceof EOFException) {
            System.out.println("Conexión cerrada por el servidor.");

            return;
        }

        MainApp.errorAlert(error);
    }


    public void stop() {
        if (clientSocket == null || !clientSocket.isConnected()) {
            System.out.println("No hay conexión activa para desconectar.");
            return;
        }

        try {
            setConnectionState(ConnectionState.DISCONNECTING);
            close();
            setConnectionState(ConnectionState.DISCONNECTED);
            System.out.println("Desconectado del servidor.");
        } catch (Exception e) {
            if (clientSocket != null && !clientSocket.isClosed()) {
                setConnectionState(ConnectionState.CONNECTED);
            } else {
                setConnectionState(ConnectionState.DISCONNECTED);
            }
            MainApp.errorAlert(e);
        }

    }


    static private String getSocketDisplayName(Socket socket) {
        if (socket == null) {
            return "null";
        }
        var address = socket.getInetAddress();
        return String.format("%s:%d", address.getHostAddress(), socket.getPort());

    }


    private void setConnectionState(ConnectionState state) {
        Platform.runLater(() -> {
            connectionStateProperty().set(state);
        });
    }

    public boolean getIsConnected() {
        return is(ConnectionState.CONNECTED).get();
    }

    public BooleanBinding is(ConnectionState state) {
        return connectionStateProperty().isEqualTo(state);
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

    private final ConsumerProperty<ChatMessageModel> onMessageReceived =
            new ConsumerProperty<>(this, "onMessageReceived");

    public void setOnMessageReceived(Consumer<ChatMessageModel> callback) {
        onMessageReceived.set(callback);
    }

    private ChatUser getUser(UUID userId) {
        return getUsers().stream().filter(user -> user.id().equals(userId)).findFirst().orElse(null);
    }

    public void sendMessage(String message, UUID receiverId) {
        if (clientSocket == null || !clientSocket.isConnected()) {
            MainApp.errorAlert("No hay conexión activa para enviar el mensaje.");
            return;
        }

        onMessageReceived.accept(new ChatMessageModel(message, toChatUser(), getUser(receiverId)));
        messageBrokerService.sender.sendMessageToServer(message, receiverId);
    }

    public String getClientName() {
        if (clientName == null || clientName.get() == null || clientName.get().isEmpty()) {
            return getSocketDisplayName(clientSocket);
        }

        return clientName.get();
    }

    public void close() throws Exception {
        if (clientSocket != null && !clientSocket.isClosed()) {
            clientSocket.close();
        }
        clientSocket = null;

        if (messageBrokerService != null) {
            messageBrokerService.close();
            messageBrokerService = null;
        }

        if (backgroundThread != null) {
            backgroundThread.interrupt();
            backgroundThread.join();
            backgroundThread = null;
        }
    }

    private StringProperty serverName = new SimpleStringProperty(this, "serverName");

    public StringProperty serverNameProperty() {
        return serverName;
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
