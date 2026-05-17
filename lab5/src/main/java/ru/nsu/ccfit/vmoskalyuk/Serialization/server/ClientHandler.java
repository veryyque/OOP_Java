package ru.nsu.ccfit.vmoskalyuk.Serialization.server;

import ru.nsu.ccfit.vmoskalyuk.Serialization.Connection;
import ru.nsu.ccfit.vmoskalyuk.Serialization.message.*;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

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
        logger.fine("ClientHandler started for: " + getVisibleName());

        try (Connection conn = new Connection(socket)) {
            this.connection = conn;
            logger.fine("Connection established for: " + getVisibleName());

            while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                Object obj = conn.read();
                logger.finest("Received object: " + obj.getClass().getSimpleName() + " from " + visibleName());
                process(obj);
            }
        } catch (java.net.SocketException e) {
            logger.fine("Socket closed for " + visibleName() + ": " + e.getMessage());
        } catch (java.io.EOFException e) {
            logger.info("Client disconnected: " + visibleName());
        } catch (java.net.SocketTimeoutException e) {
            logger.info("Client timeout: " + visibleName());
        } catch (IOException e) {
            logger.log(Level.WARNING, "IO error for " + visibleName() + ": " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, "Class not found for " + visibleName() + ": " + e.getMessage(), e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error for " + visibleName(), e);
        } finally {
            server.logout(this);
            closeSocket();
            logger.fine("ClientHandler finished for: " + getVisibleName());
            server.clientFinished(this);
        }
    }

    private void process(Object obj) {
        try {
            if (obj instanceof LoginCommand cmd) {
                logger.fine("Processing login command from: " + cmd.getName());

                List<ChatEvent> history = server.login(cmd.getName(), cmd.getType(), this);
                send(new SuccessResponse(user.session()));

                logger.fine("Sending " + history.size() + " history messages to " + user.name());
                for (ChatEvent event : history) {
                    send(event);
                }
            }
            else if (obj instanceof MessageCommand cmd) {
                logger.fine("Processing message from " + user.name()
                        + ": " + (cmd.getText().length() > 30
                        ? cmd.getText().substring(0, 27) + "..."
                        : cmd.getText()));

                server.sendMessage(user.session(), cmd.getText());
                send(new SuccessResponse(null));
            }
            else if (obj instanceof ListUsersCommand) {
                logger.fine("Processing list users command from " + user.name());
                send(server.listUsers(user.session()));
            }
            else if (obj instanceof LogoutCommand) {
                logger.info("Processing logout command from " + user.name());
                send(new SuccessResponse(null));
                socket.close();
            }
            else {
                logger.warning("Unknown object type from " + visibleName()
                        + ": " + obj.getClass().getName());
                send(new ErrorResponse("Unknown command type"));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error processing command from " + visibleName(), e);
            send(new ErrorResponse(e.getMessage()));
        }
    }

    public void send(Object obj) {
        if (connection != null) {
            try {
                connection.send(obj);
                logger.finest("Sent " + obj.getClass().getSimpleName() + " to " + visibleName());
            } catch (IOException e) {
                logger.log(Level.WARNING, "Send failed for " + visibleName(), e);
                closeSocket();
            }
        }
    }

    public ChatUser getUser() {
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
            logger.fine("Socket closed: " + getVisibleName());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error closing socket for " + getVisibleName(), e);
        }
    }

    public String getVisibleName() {
        if (user != null) {
            return user.name();
        }
        if (socket != null && socket.getRemoteSocketAddress() != null) {
            return socket.getRemoteSocketAddress().toString();
        }
        return "unknown";
    }

    private String visibleName() {
        return getVisibleName();
    }
}