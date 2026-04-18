package ru.nsu.ccfit.vmoskalyuk.Factory.model.suppliers;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Body;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;

import java.io.PrintWriter;

public class BodySupplier extends Supplier<Body> {

    public BodySupplier(Storage<Body> storage, int delayMs, PrintWriter logWriter) {
        super(storage, delayMs, "BodySupplier", logWriter);
    }

    @Override
    protected Body createDetail() {
        return new Body();
    }
}