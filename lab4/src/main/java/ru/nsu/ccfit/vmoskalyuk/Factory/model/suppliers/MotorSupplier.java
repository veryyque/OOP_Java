package ru.nsu.ccfit.vmoskalyuk.Factory.model.suppliers;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Motor;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;

import java.io.PrintWriter;

public class MotorSupplier extends Supplier<Motor> {

    public MotorSupplier(Storage<Motor> storage, int delayMs, PrintWriter logWriter) {
        super(storage, delayMs, "MotorSupplier", logWriter);
    }

    @Override
    protected Motor createDetail() {
        return new Motor();
    }
}