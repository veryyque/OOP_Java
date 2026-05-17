package ru.nsu.ccfit.vmoskalyuk.Chat.server;

import ru.nsu.ccfit.vmoskalyuk.Chat.JsonConnection;
import ru.nsu.ccfit.vmoskalyuk.Chat.JsonUtil;
import ru.nsu.ccfit.vmoskalyuk.Chat.Messages;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

//обработчик клиента
public class ClientHandler implements Runnable {
    private final ChatServer server;
    private final Socket socket;
    private JsonConnection connection;
    private ChatUser user;

    public ClientHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try (JsonConnection currentConnection = new JsonConnection(socket)) {
            connection = currentConnection;
            while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                try {
                    Map<String, Object> message = currentConnection.readMessage();
                    process(message);
                } catch (EOFException | SocketException exception) {
                    break;
                } catch (IOException | RuntimeException exception) {
                    if (Thread.currentThread().isInterrupted() || socket.isClosed()) {
                        break;
                    }
                    String message = exception.getMessage() == null || exception.getMessage().isBlank() ? "Request processing error" : exception.getMessage();
                    server.log("Client request error " + visibleName() + ": " + message);
                    send(Messages.error(message));
                    if (user == null) {
                        socket.close();
                        break;
                    }
                    if (socket.isClosed()) {
                        break;
                    }
                }
            }
        } catch (SocketTimeoutException exception) {
            server.log("Client timeout: " + visibleName());
        } catch (EOFException exception) {
            server.log("Client disconnected: " + visibleName());
        } catch (IOException | RuntimeException exception) {
            if (Thread.currentThread().isInterrupted() || socket.isClosed()) {
                return;
            }
            String message = exception.getMessage() == null || exception.getMessage().isBlank()
                    ? "Connection error"
                    : exception.getMessage();
            server.log("Client error " + visibleName() + ": " + message);
            send(Messages.error(message));
        } finally {
            server.logout(this);
            closeSocket();
            Thread.currentThread().interrupt();
            server.clientFinished(this);
        }
    }

    public synchronized void send(Map<String, Object> message) {
        if (connection == null) {
            return;
        }
        try {
            connection.sendMessage(message);
        } catch (IOException exception) {
            server.log("Cannot send to " + visibleName() + ": " + exception.getMessage());
            closeSocket();
            Thread.currentThread().interrupt();
        }
    }

    public ChatUser user() {
        return user;
    }

    public void setUser(ChatUser user) {
        this.user = user;
    }

    public void closeSocket() {
        if (socket.isClosed()) {
            return;
        }
        try {
            socket.close();
        } catch (IOException exception) {
            server.log("Cannot close client socket " + visibleName() + ": " + exception.getMessage());
        }
    }

    private void process(Map<String, Object> message) throws IOException {
        Map<String, Object> command = JsonUtil.object(message.get("command"));
        String name = JsonUtil.string(command.get("name"));
        switch (name) {
            case "login" -> handleLogin(command);
            case "list" -> send(server.listUsers(JsonUtil.string(command.get("session"))));
            case "message" -> {
                server.sendChatMessage(JsonUtil.string(command.get("session")), JsonUtil.string(command.get("message")));
                send(Messages.success());
            }
            case "logout" -> {
                server.requireUser(JsonUtil.string(command.get("session")));
                send(Messages.success());
                socket.close();
                Thread.currentThread().interrupt();
            }
            default -> send(Messages.error("Unknown command: " + name));
        }
    }

    private void handleLogin(Map<String, Object> command) throws IOException {
        List<Map<String, Object>> oldMessages = server.login(
                JsonUtil.string(command.get("user")),
                JsonUtil.string(command.get("type")),
                this
        );
        send(Messages.success("session", user.session()));
        for (Map<String, Object> oldMessage : oldMessages) {
            send(oldMessage);
        }
    }

    private String visibleName() {
        return user == null ? socket.getRemoteSocketAddress().toString() : user.name();
    }
}
