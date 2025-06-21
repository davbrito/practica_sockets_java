package com.telecomsockets.sockets;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import com.telecomsockets.MainApp;
import com.telecomsockets.models.ChatMessageModel;
import com.telecomsockets.models.ChatMessageRequest;
import com.telecomsockets.models.ChatUser;
import com.telecomsockets.services.MessageBrokerService;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public class SocketServer {

    public enum ServerState {
        LISTENING, CONNECTED, STOPPED
    }

    private ObjectProperty<ServerState> serverState = new SimpleObjectProperty<>(ServerState.STOPPED);
    public ServerSocket serverSocket;
    private final UUID serverId = UUID.randomUUID();
    private ObservableMap<UUID, ClientHandler> clientHandlers = FXCollections.observableHashMap();

    public SocketServer() {
        clientHandlers.addListener((MapChangeListener<UUID, ClientHandler>) (change) -> {
            change.getMap().values().forEach(item -> {
                item.sendServerUsers();
            });
        });

    }


    private Thread backgroundThread;

    public void start(String name, String ip, int port) {
        System.out.printf("Iniciando servidor (%s) en %s:%d%n", name, ip, port);

        if (serverSocket != null && !serverSocket.isClosed()) {
            MainApp.errorAlert("El servidor ya está en ejecución en: " + ip + ":" + port);
            return;
        }

        if (backgroundThread != null && backgroundThread.isAlive()) {
            backgroundThread.interrupt();
        }

        backgroundThread = new Thread(runServer(ip, port), "SocketServer-BackgroundThread");
        backgroundThread.start();
    }


    private Runnable runServer(String ip, int port) {
        return () -> {
            setServerState(ServerState.LISTENING);
            try {
                // Se crea un nuevo ServerSocket y se vincula a la dirección IP y puerto especificados
                serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress(ip, port));

                setServerState(ServerState.CONNECTED);

                // Inicia el servidor y comienza a escuchar conexiones entrantes
                listen(ip, port);

                setServerState(ServerState.STOPPED);
                System.out.println("Servidor detenido.");
            } catch (IOException error) {
                handleError(error);
            }
        };

    }

    /**
     * Escucha las conexiones entrantes de clientes en la dirección IP y el puerto especificados. Para
     * cada conexión entrante, se crea un nuevo hilo {@link ClientHandler} para manejar al cliente.
     *
     * @param ip La dirección IP en la que se va a escuchar.
     * @param port El número de puerto en el que se va a escuchar.
     * @throws IOException Si se produce un error de E/S al escuchar las conexiones.
     */
    private void listen(String ip, int port) throws IOException {
        System.out.printf("Servidor escuchando en %s:%d\n", ip, port);

        Socket clientSocket;

        while ((clientSocket = serverSocket.accept()) != null) {
            new ClientHandler(clientSocket).start();
        }
    }



    private void handleError(IOException error) {
        if (error instanceof SocketException && serverSocket == null) {
            return;
        }
        setServerState(ServerState.STOPPED);
        MainApp.errorAlert(error);
    }

    public ClientHandler getClientHandler(UUID clientId) {
        return clientHandlers.get(clientId);
    }

    /**
     * The name of the server.
     */
    private StringProperty serverName = new SimpleStringProperty(this, "serverName");

    public StringProperty serverNameProperty() {
        return serverName;
    }

    public ChatUser toChatUser() {
        return new ChatUser(serverId, String.format("%s (servidor)", serverName.get()));
    }

    ChatUser getChatUser(UUID id) {
        if (id.equals(serverId)) {
            return toChatUser();
        }

        var clientHandler = clientHandlers.get(id);
        if (clientHandler != null) {
            return clientHandler.toChatUser();
        }

        return null;
    }

    public void stop() throws Exception {
        close();
        setServerState(ServerState.STOPPED);
    }

    public void close() throws Exception {

        this.clientHandlers.values().stream().toList().forEach(ClientHandler::close);
        this.clientHandlers.clear();

        if (serverSocket == null)
            return;

        if (!serverSocket.isClosed()) {
            serverSocket.close();
            System.out.println("Servidor detenido.");
        } else {
            System.out.println("El servidor ya está detenido.");
        }
        serverSocket = null;

        if (backgroundThread != null && backgroundThread.isAlive()) {
            backgroundThread.interrupt();
            backgroundThread.join();
            backgroundThread = null;
        }

    }

    private void setServerState(ServerState state) {
        Platform.runLater(() -> {
            serverState.set(state);
        });
    }

    public ObjectProperty<ServerState> serverStateProperty() {
        return serverState;
    }

    public ObservableMap<UUID, ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    public boolean getIsStopped() {
        return serverState.get() == ServerState.STOPPED;
    }

    public BooleanBinding is(ServerState state) {
        return serverStateProperty().isEqualTo(state);
    }

    public UUID getServerId() {
        return serverId;
    }

    private Consumer<ChatMessageModel> onMessageReceived;

    public void setOnMessageReceived(Consumer<ChatMessageModel> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    private void notifyMessageReceived(ChatMessageModel message) {
        if (onMessageReceived != null) {
            onMessageReceived.accept(message);
        }
    }

    public void sendMessageToReceiver(ChatMessageRequest message, ChatUser sender) {
        var receiverId = message.receiverId();
        var text = message.text();

        var receiver = getChatUser(receiverId);

        if (receiver == null) {
            MainApp.errorAlert("El receptor no esta conectado: " + receiverId);
            return;
        }

        sendMessageToReceiver(text, sender, receiver);
    }

    public void sendMessageToReceiver(String messageText, ChatUser sender, ChatUser receiver) {
        var messageModel = new ChatMessageModel(messageText, sender, receiver);

        if (receiver.id().equals(serverId) || sender.id().equals(serverId)) {
            notifyMessageReceived(messageModel);

            if (receiver.id().equals(serverId)) {
                // If the receiver is the server, we don't need to send it back to the server
                return;
            }
        }

        var client = getClientHandler(receiver.id());

        if (client == null) {
            MainApp.errorAlert("El receptor no está conectado: " + receiver.id());
            return;
        }

        client.messageBrokerService.sender.sendMessageToReceiver(messageModel);
    }


    public class ClientHandler {

        private Socket clientSocket;
        private UUID clientId;
        private StringProperty clientName = new SimpleStringProperty(this, "clientName", "<Sin nombre>");
        private MessageBrokerService messageBrokerService;

        private ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }


        public void start() {
            new Thread(this::run, "ClientHandler[" + getClientName() + "]").start();
        }


        private void run() {
            try {
                messageBrokerService = new MessageBrokerService(clientSocket, getClientId());

                System.out.println("Cliente conectado: " + getClientAddress());


                // Se setean los manejadores de eventos para el servicio de mensajería
                messageBrokerService.receiver.setOnHandshake(handshake -> {
                    var clientId = handshake.id();
                    var clientName = handshake.name();
                    this.clientId = clientId;
                    this.clientName.set(clientName);
                    System.out.println("Handshake recibido: " + clientName + " (" + clientId + ")");
                    SocketServer.this.clientHandlers.put(clientId, this);
                    messageBrokerService.sender.sendHandShake(serverId, serverNameProperty().get());

                });
                messageBrokerService.receiver.setOnMessageReceived(SocketServer.this::notifyMessageReceived);
                messageBrokerService.receiver.setOnRequestClientList(this::sendServerUsers);
                messageBrokerService.receiver.setOnMessageRequest(this::forwardMessage);

                // Se inicia el servicio de mensajería para recibir mensajes
                messageBrokerService.receiver.receiveMessages();
            } catch (ClassNotFoundException | IOException e) {
                handleError(e);
            }
        }



        private void handleError(Throwable error) {
            close();

            if (error instanceof SocketException && clientSocket == null) {
                return;
            }
            if (error instanceof EOFException) {
                System.out.println("Conexión cerrada por el cliente.");
                return;
            }

            MainApp.errorAlert(error);
        }

        public ReadOnlyStringProperty clientNameProperty() {
            return clientName;
        }

        public UUID getClientId() {
            return clientId;
        }

        public String getClientName() {
            return clientNameProperty().get();
        }

        public String getClientAddress() {
            var address = clientSocket.getInetAddress();
            return String.format("%s:%d", address.getHostAddress(), clientSocket.getPort());
        }


        void forwardMessage(ChatMessageRequest message) {
            SocketServer.this.sendMessageToReceiver(message, this.toChatUser());
        }

        protected void sendServerUsers(ObservableMap<UUID, ClientHandler> map) {
            var userList = map.values().stream()
                    // Exclude self
                    .filter(item -> item.getClientId() != this.clientId)
                    .map(item -> new ChatUser(item.getClientId(), item.getClientName()));
            messageBrokerService.sender
                    .sendUserListResponse(Stream.concat(Stream.of(SocketServer.this.toChatUser()), userList).toList());
        }

        protected void sendServerUsers() {
            sendServerUsers(SocketServer.this.clientHandlers);
        }

        void close() {
            Platform.runLater(() -> {
                SocketServer.this.clientHandlers.remove(this.clientId);
            });

            if (clientSocket == null)
                return;

            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                    System.out.println("Cliente desconectado.");
                } else {
                    System.out.println("El cliente ya está desconectado.");
                }

                clientSocket = null;

                messageBrokerService.close();

            } catch (Exception e) {
                MainApp.errorAlert(e, "Error al cerrar el socket");
            }

        }

        public ChatUser toChatUser() {
            return new ChatUser(clientId, clientName.get());
        }
    }

}
