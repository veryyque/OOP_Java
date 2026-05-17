package ru.nsu.ccfit.vmoskalyuk.Serialization.server;

import ru.nsu.ccfit.vmoskalyuk.Chat.ServerConfig;
import ru.nsu.ccfit.vmoskalyuk.Serialization.message.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ChatServer {
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    private final ServerConfig config;
    private final Map<String, ChatUser> usersBySession = new HashMap<>();
    private final Map<String, ChatUser> usersByName = new HashMap<>();
    private final LinkedList<ChatEvent> history = new LinkedList<>();
    private final List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>());
    private final List<Thread> clientThreads = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean running = true;
    private volatile boolean shutdownStarted;
    private ServerSocket serverSocket;

    public ChatServer(ServerConfig config) {
        this.config = config;
    }

    public static void main(String[] args) throws IOException {

        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$s] %5$s%6$s%n");
        try {
            String logFilePath = "server.log";
            FileHandler fileHandler = new FileHandler(logFilePath, true);
            fileHandler.setFormatter(new SimpleFormatter());

            Logger rootLogger = Logger.getLogger("");
            rootLogger.addHandler(fileHandler);

            rootLogger.setLevel(Level.ALL);
            fileHandler.setLevel(Level.ALL);

            java.io.File logFile = new java.io.File(logFilePath);
            System.out.println("=== LOG FILE: " + logFile.getAbsolutePath() + " ===");
        } catch (IOException e) {
            System.err.println("Cannot create log file: " + e.getMessage());
        }
        ServerConfig config = ServerConfig.load(args);
        ChatServer server = new ChatServer(config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("=== JVM SHUTDOWN HOOK TRIGGERED ===");
            server.shutdown();
        }, "shutdown-hook"));

        try {
            server.start();
        } catch (IOException exception) {
            if (server.running) {
                logger.log(Level.SEVERE, "Server error: " + exception.getMessage(), exception);
            }
        } finally {
            server.shutdown();
        }
    }

    public void start() throws IOException {
        logger.info("Serialization Server started on port " + config.port());
        logger.info("Logging enabled: " + config.loggingEnabled());
        logger.info("History size: " + config.historySize());
        logger.info("Client timeout: " + config.clientTimeoutMs() + "ms");

        try (ServerSocket currentServerSocket = new ServerSocket(config.port())) {
            serverSocket = currentServerSocket;
            logger.info("Server socket created, waiting for connections...");

            while (running && !currentServerSocket.isClosed()) {
                Socket socket = currentServerSocket.accept();
                socket.setSoTimeout(config.clientTimeoutMs());

                logger.info("New connection from: " + socket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(this, socket);
                Thread thread = new Thread(handler, "client-" + socket.getPort());
                clientHandlers.add(handler);
                clientThreads.add(thread);

                logger.fine("Client thread created: " + thread.getName() + " | Total active threads: " + clientThreads.size());

                thread.start();
            }
        }
        logger.info("Server main loop exited");
    }
    public void shutdown() {
        if (shutdownStarted) return;
        shutdownStarted = true;
        running = false;

        System.out.println("\n[SHUTDOWN] Начинаю остановку сервера...");

        closeServerSocket();
        System.out.println("[SHUTDOWN] Серверный сокет закрыт.");

        List<ClientHandler> handlers;
        synchronized (clientHandlers) {
            handlers = new ArrayList<>(clientHandlers);
        }

        System.out.println("[SHUTDOWN] Закрываю сокеты клиентов: " + handlers.size());
        for (ClientHandler handler : handlers) {
            handler.closeSocket();
        }

        synchronized (clientThreads) {
            System.out.println("[SHUTDOWN] Прерываю потоки: " + clientThreads.size());
            for (Thread t : clientThreads) {
                t.interrupt();
            }

            for (Thread t : clientThreads) {
                try {
                    if (t != Thread.currentThread()) t.join(100);
                } catch (InterruptedException ignored) {}
            }
        }

        System.out.println("[SHUTDOWN] Потоков осталось: " + clientThreads.size());
        System.out.println("[SHUTDOWN] Обработчиков осталось: " + clientHandlers.size());

        clientThreads.clear();
        clientHandlers.clear();

        System.out.println("[SHUTDOWN] ВСЁ ЗАКРЫТО. Списки очищены.");
        System.out.flush();
    }

    public void clientFinished(ClientHandler handler) {
        String name = handler.getVisibleName();
        clientHandlers.remove(handler);
        clientThreads.remove(Thread.currentThread());
        logger.fine("Client finished: " + name + " | Remaining threads: " + clientThreads.size());
    }

    private void closeServerSocket() {
        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }
        try {
            serverSocket.close();
            logger.fine("Server socket closed successfully");
        } catch (IOException exception) {
            logger.log(Level.WARNING, "Error closing server socket", exception);
        }
    }

    public synchronized List<ChatEvent> login(String name, String type, ClientHandler handler) throws IOException {
        if (name == null || name.isBlank()) {
            logger.warning("Login rejected: empty name");
            throw new IOException("Name is empty");
        }
        if (usersByName.containsKey(name)) {
            logger.warning("Login rejected: name '" + name + "' is already used");
            throw new IOException("Name is already used");
        }

        String session = UUID.randomUUID().toString();
        ChatUser user = new ChatUser(session, name.trim(), type == null || type.isBlank() ? "Java Serialization Client" : type, handler);

        usersBySession.put(session, user);
        usersByName.put(user.name(), user);
        handler.setUser(user);

        logger.info("User logged in: " + user.name() + " | Type: " + user.type() + " | Session: " + session.substring(0, 8) + "..." + " | Online users: " + usersBySession.size());

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

        logger.info("User logged out: " + user.name() + " | Online users remaining: " + usersBySession.size());

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

        logger.info("Message from " + user.name() + ": " + (text.length() > 50 ? text.substring(0, 47) + "..." : text));

        broadcast(event);
    }

    public synchronized UserListResponse listUsers(String session) throws IOException {
        requireUser(session);
        List<UserInfo> users = new ArrayList<>();
        for (ChatUser user : usersBySession.values()) {
            users.add(new UserInfo(user.name(), user.type()));
        }
        logger.fine("User list requested | Online: " + users.size());
        return new UserListResponse(users);
    }

    public synchronized ChatUser requireUser(String session) throws IOException {
        ChatUser user = usersBySession.get(session);
        if (user == null) {
            logger.warning("Unknown session attempted: " + session);
            throw new IOException("Unknown session");
        }
        return user;
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