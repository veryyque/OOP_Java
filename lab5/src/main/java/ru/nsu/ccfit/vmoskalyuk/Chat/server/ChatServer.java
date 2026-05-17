package ru.nsu.ccfit.vmoskalyuk.Chat.server;

import ru.nsu.ccfit.vmoskalyuk.Chat.Messages;
import ru.nsu.ccfit.vmoskalyuk.Chat.ServerConfig;

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
    private final ArrayDeque<Map<String, Object>> history = new ArrayDeque<>();
    private final List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>());
    private final List<Thread> clientThreads = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean running = true;
    private volatile boolean shutdownStarted;
    private ServerSocket serverSocket;

    public ChatServer(ServerConfig config) {
        this.config = config;
    }

    public static void main(String[] args) {
        ServerConfig config = ServerConfig.load(args);
        ChatServer server = new ChatServer(config);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("=== JVM SHUTDOWN HOOK TRIGGERED ===");
            server.shutdown();
        }, "shutdown-hook"));
        Thread consoleThread = new Thread(server::waitForShutdownCommand, "chat-server-console");
        consoleThread.start();
        try {
            server.start();
        } catch (IOException exception) {
            if (server.running) {
                System.err.println("Server error: " + exception.getMessage());
            }
        } finally {
            server.shutdown();
        }
    }

    public void start() throws IOException {
        log("Server started on port " + config.port());
        try (ServerSocket currentServerSocket = new ServerSocket(config.port())) {
            serverSocket = currentServerSocket;
            while (running && !currentServerSocket.isClosed()) {
                Socket socket = currentServerSocket.accept();
                socket.setSoTimeout(config.clientTimeoutMs());
                log("Connection from " + socket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(this, socket);
                Thread thread = new Thread(handler, "client-" + socket.getPort());
                clientHandlers.add(handler);
                clientThreads.add(thread);
                log("Active client threads: " + clientThreads.size());
                thread.start();
            }
        }
    }

    public void shutdown() {
        if (shutdownStarted) {
            return;
        }
        shutdownStarted = true;
        running = false;
        closeServerSocket();

        List<ClientHandler> handlersCopy;
        synchronized (clientHandlers) {
            handlersCopy = new ArrayList<>(clientHandlers);
        }
        for (ClientHandler handler : handlersCopy) {
            handler.closeSocket();
        }

        List<Thread> threadsCopy;
        synchronized (clientThreads) {
            threadsCopy = new ArrayList<>(clientThreads);
        }
        for (Thread thread : threadsCopy) {
            thread.interrupt();
        }
        for (Thread thread : threadsCopy) {
            if (thread == Thread.currentThread()) {
                continue;
            }
            try {
                thread.join(1000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        synchronized (clientThreads) {
            clientThreads.clear();
        }
        synchronized (clientHandlers) {
            clientHandlers.clear();
        }
        log("Server stopped. Active client threads: " + clientThreads.size());
    }

    public void clientFinished(ClientHandler handler) {
        clientHandlers.remove(handler);
        clientThreads.remove(Thread.currentThread());
        log("Client thread finished. Active client threads: " + clientThreads.size());
    }

    private void waitForShutdownCommand() {
        Scanner scanner = new Scanner(System.in);
        while (running && scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            if ("stop".equalsIgnoreCase(command) || "exit".equalsIgnoreCase(command)) {
                shutdown();
                return;
            }
        }
    }

    private void closeServerSocket() {
        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }
        try {
            serverSocket.close();
        } catch (IOException exception) {
            log("Cannot close server socket: " + exception.getMessage());
        }
    }

    public synchronized List<Map<String, Object>> login(String name, String type, ClientHandler handler) throws IOException {
        if (name == null || name.isBlank()) {
            throw new IOException("Name is empty");
        }
        if (usersByName.containsKey(name)) {
            throw new IOException("Name is already used");
        }
        String session = UUID.randomUUID().toString();
        ChatUser user = new ChatUser(session, name.trim(), type == null || type.isBlank() ? "Java Swing JSON Client" : type, handler);
        usersBySession.put(session, user);
        usersByName.put(user.name(), user);
        handler.setUser(user);
        log(user.name() + " logged in");
        broadcastExcept(Messages.event("userlogin", "user", user.name()), session);
        return new ArrayList<>(history);
    }

    public synchronized void logout(ClientHandler handler) {
        ChatUser user = handler.user();
        if (user == null || !usersBySession.containsKey(user.session())) {
            return;
        }
        usersBySession.remove(user.session());
        usersByName.remove(user.name());
        log(user.name() + " logged out");
        broadcastExcept(Messages.event("userlogout", "user", user.name()), user.session());
        handler.setUser(null);
    }

    public synchronized void sendChatMessage(String session, String text) throws IOException {
        ChatUser user = requireUser(session);
        if (text == null || text.isBlank()) {
            throw new IOException("Message is empty");
        }
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Map<String, Object> event = Messages.event("message", "message", text, "user", user.name(), "time", time);
        remember(event);
        log(user.name() + ": " + text);
        broadcast(event);
    }

    public synchronized Map<String, Object> listUsers(String session) throws IOException {
        requireUser(session);
        List<Object> users = new ArrayList<>();
        for (ChatUser user : usersBySession.values()) {
            users.add(Messages.user(user.name(), user.type()));
        }
        return Messages.success("listusers", users);
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

    private void remember(Map<String, Object> event) {
        history.addLast(event);
        while (history.size() > config.historySize()) {
            history.removeFirst();
        }
    }

    private void broadcast(Map<String, Object> message) {
        for (ChatUser user : usersBySession.values()) {
            user.handler().send(message);
        }
    }

    private void broadcastExcept(Map<String, Object> message, String session) {
        for (ChatUser user : usersBySession.values()) {
            if (!user.session().equals(session)) {
                user.handler().send(message);
            }
        }
    }
}
