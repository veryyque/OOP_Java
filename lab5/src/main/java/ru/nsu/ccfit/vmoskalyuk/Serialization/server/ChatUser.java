package ru.nsu.ccfit.vmoskalyuk.Serialization.server;

public record ChatUser(String session, String name, String type, ClientHandler handler) {
}