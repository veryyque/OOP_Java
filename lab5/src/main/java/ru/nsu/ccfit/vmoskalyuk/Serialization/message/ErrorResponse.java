package ru.nsu.ccfit.vmoskalyuk.Serialization.message;

import java.io.Serial;

public class ErrorResponse implements ChatProtocol {
    @Serial
    private static final long serialVersionUID = 2;
    private final String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
}
