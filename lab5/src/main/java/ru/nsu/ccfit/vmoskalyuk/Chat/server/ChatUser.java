package ru.nsu.ccfit.vmoskalyuk.Chat.server;

public record ChatUser(String session, String name, String type, ClientHandler handler) {
}
