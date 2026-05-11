package ru.nsu.ccfit.vmoskalyuk.Serialization.client;

import ru.nsu.ccfit.vmoskalyuk.Serialization.Connection;
import ru.nsu.ccfit.vmoskalyuk.Serialization.message.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.EOFException;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClientConnection implements Closeable {
    private final Connection connection;
    private final Consumer<Object> listener;
    private volatile boolean running = true;
    private String session;

    public ChatClientConnection(String host, int port, Consumer<Object> listener) throws IOException {
        this.connection = new Connection(new Socket(host, port));
        this.listener = listener;
    }

    public void login(String name) throws IOException {
        connection.send(new LoginCommand(name, "Java Serialization Client"));

        try {
            Object response = connection.read();

            if (response instanceof SuccessResponse success) {
                this.session = success.getSession();
                startReader();
            } else if (response instanceof ErrorResponse error) {
                throw new IOException(error.getMessage());
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found: " + e.getMessage());
        }
    }

    public void sendMessage(String text) throws IOException {
        if (session == null) throw new IOException("Not logged in");
        connection.send(new MessageCommand(text));
    }

    public void requestUsers() throws IOException {
        if (session == null) throw new IOException("Not logged in");
        connection.send(new ListUsersCommand());
    }

    public void logout() throws IOException {
        if (session != null && running) {
            connection.send(new LogoutCommand());
        }
        close();
    }

    private void startReader() {
        Thread reader = new Thread(() -> {
            while (running) {
                try {
                    Object message = connection.read();
                    listener.accept(message);
                } catch (EOFException e) {
                    if (running) {
                        listener.accept(new ErrorResponse("Connection closed"));
                    }
                    running = false;
                } catch (IOException | ClassNotFoundException e) {
                    if (running) {
                        listener.accept(new ErrorResponse(e.getMessage()));
                    }
                    running = false;
                }
            }
        }, "server-reader");
        reader.setDaemon(true); //для точного завершения потока если не нажать disconnect и закрыть окно
        reader.start();
    }

    @Override
    public void close() throws IOException {
        running = false;
        connection.close();
    }

    public boolean isConnected() {
        return session != null && running;
    }
}
