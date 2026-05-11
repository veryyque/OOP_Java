package ru.nsu.ccfit.vmoskalyuk.Serialization.server;

import ru.nsu.ccfit.vmoskalyuk.Chat.ServerConfig;
import ru.nsu.ccfit.vmoskalyuk.Serialization.message.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatServer {
    private final ServerConfig config;
    private final Map<String, ChatUser> usersBySession = new HashMap<>();
    private final Map<String, ChatUser> usersByName = new HashMap<>();
    private final LinkedList<ChatEvent> history = new LinkedList<>();

    public ChatServer(ServerConfig config) {
        this.config = config;
    }

    public static void main(String[] args) {
        ServerConfig config = ServerConfig.load(args);
        try {
            new ChatServer(config).start();
        } catch (IOException exception) {
            System.err.println("Server error: " + exception.getMessage());
        }
    }

    public void start() throws IOException {
        log("Serialization Server started on port " + config.port());
        try (ServerSocket serverSocket = new ServerSocket(config.port())) {
            while (true) {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(config.clientTimeoutMs());
                log("Connection from " + socket.getRemoteSocketAddress());
                new Thread(new ClientHandler(this, socket), "client-" + socket.getPort()).start();
            }
        }
    }

    public synchronized List<ChatEvent> login(String name, String type, ClientHandler handler) throws IOException {
        if (name == null || name.isBlank()) {
            throw new IOException("Name is empty");
        }
        if (usersByName.containsKey(name)) {
            throw new IOException("Name is already used");
        }

        String session = UUID.randomUUID().toString();
        ChatUser user = new ChatUser(session, name.trim(), type == null || type.isBlank() ? "Java Serialization Client" : type, handler);

        usersBySession.put(session, user);
        usersByName.put(user.name(), user);
        handler.setUser(user);

        log(user.name() + " logged in");
        broadcastExcept(new ChatEvent("userlogin", user.name(), null), session);

        return new ArrayList<>(history);
    }

    public synchronized void logout(ClientHandler handler) {
        ChatUser user = handler.getUser();
        if (user == null || !usersBySession.containsKey(user.session())) {
            return;
        }
        usersBySession.remove(user.session());
        usersByName.remove(user.name());
        log(user.name() + " logged out");
        broadcastExcept(new ChatEvent("userlogout", user.name(), null), user.session());
        handler.setUser(null);
    }

    public synchronized void sendMessage(String session, String text) throws IOException {
        ChatUser user = requireUser(session);
        if (text == null || text.isBlank()) {
            throw new IOException("Message is empty");
        }

        ChatEvent event = new ChatEvent("message", user.name(), text);
        remember(event);
        log(user.name() + ": " + text);
        broadcast(event);
    }

    public synchronized UserListResponse listUsers(String session) throws IOException {
        requireUser(session);
        List<UserInfo> users = new ArrayList<>();
        for (ChatUser user : usersBySession.values()) {
            users.add(new UserInfo(user.name(), user.type()));
        }
        return new UserListResponse(users);
    }

    public synchronized ChatUser requireUser(String session) throws IOException {
        ChatUser user = usersBySession.get(session);
        if (user == null) {
            throw new IOException("Unknown session");
        }
        return user;
    }

    public void log(String message) {
        if (config.loggingEnabled()) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            System.out.println("[" + time + "] " + message);
        }
    }

    private void remember(ChatEvent event) {
        history.addLast(event);
        while (history.size() > config.historySize()) {
            history.removeFirst();
        }
    }

    private void broadcast(ChatProtocol message) {
        for (ChatUser user : usersBySession.values()) {
            user.handler().send(message);
        }
    }

    private void broadcastExcept(ChatProtocol message, String session) {
        for (ChatUser user : usersBySession.values()) {
            if (!user.session().equals(session)) {
                user.handler().send(message);
            }
        }
    }
}