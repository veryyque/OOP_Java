package ru.nsu.ccfit.vmoskalyuk.Serialization.message;

import java.io.Serial;

public class SuccessResponse implements ChatProtocol {
    @Serial
    private static final long serialVersionUID = 7;
    private final String session;

    public SuccessResponse(String session) {
        this.session = session;
    }

    public String getSession() { return session; }
}
