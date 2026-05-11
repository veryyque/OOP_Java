package ru.nsu.ccfit.vmoskalyuk.Serialization.message;

import java.io.Serial;
import java.time.LocalDateTime;

public class ChatEvent implements ChatProtocol {
    @Serial
    private static final long serialVersionUID = 1;

    private final String eventType; // message,user login,user logout
    private final String user;
    private final String text;
    private final LocalDateTime timestamp;

    public ChatEvent(String eventType, String user, String text) {
        this.eventType = eventType;
        this.user = user;
        this.text = text;
        this.timestamp = LocalDateTime.now();
    }

    public String getEventType() { return eventType; }
    public String getUser() { return user; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
