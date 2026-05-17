package ru.nsu.ccfit.vmoskalyuk.Chat.client;


import ru.nsu.ccfit.vmoskalyuk.Chat.JsonConnection;
import ru.nsu.ccfit.vmoskalyuk.Chat.JsonUtil;
import ru.nsu.ccfit.vmoskalyuk.Chat.Messages;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.function.Consumer;

public class ChatClientConnection implements Closeable {
    private final JsonConnection connection;
    private final Consumer<Map<String, Object>> listener; //функциональный интерфейс
    private volatile boolean running = true;
    private String session;
    private Thread readerThread;

    public ChatClientConnection(String host, int port, Consumer<Map<String, Object>> listener) throws IOException {
        this.connection = new JsonConnection(new Socket(host, port));
        this.listener = listener;
    }

    public void login(String name) throws IOException {
        connection.sendMessage(Messages.command("login", "user", name, "type", "Java Swing JSON Client"));
        Map<String, Object> answer = connection.readMessage();
        checkError(answer);
        Map<String, Object> success = JsonUtil.object(answer.get("success"));
        session = JsonUtil.string(success.get("session"));
        if (session.isBlank()) {
            throw new IOException("Server did not return session");
        }
        startReader();
    }

    public void sendText(String text) throws IOException {
        requireSession();
        connection.sendMessage(Messages.command("message", "message", text, "session", session));
    }

    public void requestUsers() throws IOException {
        requireSession();
        connection.sendMessage(Messages.command("list", "session", session));
    }

    public void logout() throws IOException {
        if (session != null && running) {
            connection.sendMessage(Messages.command("logout", "session", session));
        }
        close();
    }

    private void startReader() {
        readerThread = new Thread(() -> {
            while (running) {
                try {
                    Map<String, Object> message = connection.readMessage();
                    checkError(message);
                    listener.accept(message);
                } catch (EOFException exception) {
                    if (running) {
                        listener.accept(Messages.error("Connection closed"));
                    }
                    running = false;
                } catch (IOException | RuntimeException exception) {
                    if (running) {
                        String message = exception.getMessage() == null || exception.getMessage().isBlank()
                                ? "Connection error"
                                : exception.getMessage();
                        listener.accept(Messages.error(message));
                    }
                    running = false;
                }
            }
        }, "server-reader");
        readerThread.start();
    }

    @Override
    public void close() throws IOException {
        running = false;
        connection.close();
        if (readerThread != null && readerThread != Thread.currentThread()) {
            readerThread.interrupt();
        }
    }

    private void checkError(Map<String, Object> message) throws IOException {
        Map<String, Object> error = JsonUtil.object(message.get("error"));
        String errorText = JsonUtil.string(error.get("message"));
        if (!errorText.isBlank()) {
            throw new IOException(errorText);
        }
    }

    private void requireSession() throws IOException {
        if (session == null || session.isBlank()) {
            throw new IOException("Not connected");
        }
    }
}
