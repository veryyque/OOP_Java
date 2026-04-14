package ru.nsu.ccfit.vmoskalyuk.Factory.model.suppliers;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Motor;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;

public class MotorSupplier extends Supplier<Motor> {
    public MotorSupplier(Storage<Motor> storage, int delayMs) {
        super(storage, delayMs, "MotorSupplier");
    }

    @Override
    protected Motor createDetail() {
        return new Motor();
    }
}