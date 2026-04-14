package ru.nsu.ccfit.vmoskalyuk.Factory.model.suppliers;

import ru.nsu.ccfit.vmoskalyuk.Factory.model.details.Accessory;
import ru.nsu.ccfit.vmoskalyuk.Factory.model.storage.Storage;

public class AccessorySupplier extends Supplier<Accessory> {
    private static int counter = 0;

    public AccessorySupplier(Storage<Accessory> storage, int delayMs) {
        super(storage, delayMs, "AccessorySupplier-" + (++counter));
    }

    @Override
    protected Accessory createDetail() {
        return new Accessory();
    }
}