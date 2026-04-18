// ru/nsu/ccfit/vmoskalyuk/Factory/model/details/Car.java
package ru.nsu.ccfit.vmoskalyuk.Factory.model.details;

public class Car {
    private static long idCounter = 0;

    private final long id;
    private final Body body;
    private final Motor motor;
    private final Accessory accessory;

    public Car(Body body, Motor motor, Accessory accessory) {
        // Проверка на null
        if (body == null || motor == null || accessory == null) {
            throw new IllegalArgumentException("Все три детали должны быть не null");
        }

        if (body.isUsed() || motor.isUsed() || accessory.isUsed()) {
            throw new IllegalStateException("Попытка использовать уже занятую деталь в новой машине!");
        }

        synchronized (Car.class) {
            this.id = ++idCounter;
        }

        body.markAsUsed();
        motor.markAsUsed();
        accessory.markAsUsed();

        this.body = body;
        this.motor = motor;
        this.accessory = accessory;
    }

    public long getId() {
        return id;
    }

    public Body getBody() {
        return body;
    }

    public Motor getMotor() {
        return motor;
    }

    public Accessory getAccessory() {
        return accessory;
    }

    @Override
    public String toString() {
        return String.format("Car-%d (Body:%d, Motor:%d, Accessory:%d)",
                id, body.getId(), motor.getId(), accessory.getId());
    }
}