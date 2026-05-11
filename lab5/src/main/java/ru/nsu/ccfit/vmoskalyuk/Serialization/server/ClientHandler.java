package ru.nsu.ccfit.vmoskalyuk.Serialization.server;

import ru.nsu.ccfit.vmoskalyuk.Serialization.Connection;
import ru.nsu.ccfit.vmoskalyuk.Serialization.message.*;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final ChatServer server;
    private final Socket socket;
    private Connection connection;
    private ChatUser user;

    public ClientHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Connection conn = new Connection(socket)) {
            this.connection = conn;
            while (!socket.isClosed()) {
                Object obj = conn.read();
                process(obj);
            }
        } catch (Exception e) {
            server.log("Client error " + visibleName() + ": " + e.getMessage());
        } finally {
            server.logout(this);
        }
    }

    private void process(Object obj) {
        try {
            if (obj instanceof LoginCommand cmd) {
                List<ChatEvent> history = server.login(cmd.getName(), cmd.getType(), this);
                send(new SuccessResponse(user.session()));
                for (ChatEvent event : history) {
                    send(event);
                }
            }
            else if (obj instanceof MessageCommand cmd) {
                server.sendMessage(user.session(), cmd.getText());
                send(new SuccessResponse(null));
            }
            else if (obj instanceof ListUsersCommand) {
                send(server.listUsers(user.session()));
            }
            else if (obj instanceof LogoutCommand) {
                send(new SuccessResponse(null));
                socket.close();
            }
        } catch (Exception e) {
            send(new ErrorResponse(e.getMessage()));
        }
    }

    public void send(Object obj) {
        if (connection != null) {
            try {
                connection.send(obj);
            } catch (IOException _) {
            }
        }
    }

    public ChatUser getUser() { return user; }
    public void setUser(ChatUser user) { this.user = user; }

    private String visibleName() {
        return user != null ? user.name() : socket.getRemoteSocketAddress().toString();
    }
}
