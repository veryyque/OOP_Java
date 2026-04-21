package ru.nsu.ccfit.vmoskalyuk.Factory.config;

import java.io.InputStream;
import java.util.Properties;

public class Config {
    private final Properties props = new Properties();

    public Config() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("factory.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                setDefaults();
            }
        } catch (Exception e) {
            setDefaults();
        }
    }

    private void setDefaults() {
        props.setProperty("storage.body.size", "50");
        props.setProperty("storage.motor.size", "50");
        props.setProperty("storage.accessory.size", "50");
        props.setProperty("storage.car.size", "50");
        props.setProperty("suppliers.accessory.count", "3");
        props.setProperty("workers.count", "5");
        props.setProperty("dealers.count", "3");
        props.setProperty("delay.body", "1000");
        props.setProperty("delay.motor", "1000");
        props.setProperty("delay.accessory", "1000");
        props.setProperty("delay.dealer", "1000");
        props.setProperty("log.sale", "true");
    }

    public int getInt(String key) {
        return Integer.parseInt(props.getProperty(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(props.getProperty(key));
    }
}