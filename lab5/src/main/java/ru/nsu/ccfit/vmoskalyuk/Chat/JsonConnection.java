package ru.nsu.ccfit.vmoskalyuk.Chat;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonConnection implements Closeable {
    private static final int MAX_MESSAGE_SIZE = 1024; //byte

    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;

    public JsonConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    public Map<String, Object> readMessage() throws IOException {
        int length;
        length = input.readInt(); //прочитать начальные 4 байта
        if (length <= 0 || length > MAX_MESSAGE_SIZE) {
            throw new IOException("Incorrect JSON message length: " + length);
        }
        byte[] bytes = input.readNBytes(length);
        if (bytes.length != length) {
            throw new EOFException("Connection closed while reading JSON");
        }
        return JsonUtil.parseObject(new String(bytes, StandardCharsets.UTF_8));
    }

    public synchronized void sendMessage(Map<String, Object> message) throws IOException {
        byte[] bytes = JsonUtil.stringify(message).getBytes(StandardCharsets.UTF_8);
        output.writeInt(bytes.length);
        output.write(bytes);
        output.flush();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
