package ru.nsu.ccfit.vmoskalyuk.Serialization;

import java.io.*;
import java.net.Socket;

public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Object obj) throws IOException { //сериализация объекта
        out.writeObject(obj);
        out.flush();
    }

    public Object read() throws IOException, ClassNotFoundException { //десериализация
        return in.readObject();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}