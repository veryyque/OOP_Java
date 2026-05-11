package ru.nsu.ccfit.vmoskalyuk.Serialization.message;

import java.io.Serial;

public class LoginCommand implements ChatProtocol {
    @Serial
    private static final long serialVersionUID = 4;
    private final String name;
    private final String type;

    public LoginCommand(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public String getType() { return type; }
}