package ru.nsu.ccfit.vmoskalyuk.Serialization.message;

import java.io.Serial;
import java.io.Serializable;

public class UserInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 8;
    private final String name;
    private final String type;

    public UserInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public String getType() { return type; }
}
