package ru.nsu.ccfit.vmoskalyuk.Factory.model.suppliers;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Accessory;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;

import java.io.PrintWriter;

public class AccessorySupplier extends Supplier<Accessory> {

    public AccessorySupplier(Storage<Accessory> storage, int delayMs, int supplierNumber, PrintWriter logWriter) {
        super(storage, delayMs, "AccessorySupplier-" + supplierNumber, logWriter);
    }

    @Override
    protected Accessory createDetail() {
        return new Accessory();
    }
}