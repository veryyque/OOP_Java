package ru.nsu.ccfit.vmoskalyuk.Chat.server;

import ru.nsu.ccfit.vmoskalyuk.Chat.JsonConnection;
import ru.nsu.ccfit.vmoskalyuk.Chat.JsonUtil;
import ru.nsu.ccfit.vmoskalyuk.Chat.Messages;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
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
            while (!socket.isClosed()) {
                try {
                    process(currentConnection.readMessage());
                } catch (IOException | RuntimeException exception) {
                    String message = exception.getMessage() == null || exception.getMessage().isBlank()
                            ? "Request processing error"
                            : exception.getMessage();
                    server.log("Client request error " + visibleName() + ": " + message);
                    send(Messages.error(message));
                    if (user == null) {
                        socket.close();
                    }
                }
            }
        } catch (SocketTimeoutException exception) {
            server.log("Client timeout: " + visibleName());
        } catch (EOFException exception) {
            server.log("Client disconnected: " + visibleName());
        } catch (IOException | RuntimeException exception) {
            String message = exception.getMessage() == null || exception.getMessage().isBlank()
                    ? "Connection error"
                    : exception.getMessage();
            server.log("Client error " + visibleName() + ": " + message);
            send(Messages.error(message));
        } finally {
            server.logout(this);
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
        }
    }

    public ChatUser user() {
        return user;
    }

    public void setUser(ChatUser user) {
        this.user = user;
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
