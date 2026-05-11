package ru.nsu.ccfit.vmoskalyuk.Serialization.message;

import java.io.Serial;

public class MessageCommand implements ChatProtocol {
    @Serial
    private static final long serialVersionUID = 6;
    private final String text;

    public MessageCommand(String text) {
        this.text = text;
    }

    public String getText() { return text; }
}
