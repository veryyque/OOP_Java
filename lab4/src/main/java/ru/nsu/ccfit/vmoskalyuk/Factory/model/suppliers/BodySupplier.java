package ru.nsu.ccfit.vmoskalyuk.Factory.model.suppliers;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Body;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;

public class BodySupplier extends Supplier<Body> {
    public BodySupplier(Storage<Body> storage, int delayMs) {
        super(storage, delayMs, "BodySupplier");
    }

    @Override
    protected Body createDetail() {
        return new Body();
    }
}