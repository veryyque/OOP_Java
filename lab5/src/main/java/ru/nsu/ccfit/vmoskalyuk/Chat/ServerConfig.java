package ru.nsu.ccfit.vmoskalyuk.Chat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ServerConfig {
    private final int port;
    private final boolean loggingEnabled;
    private final int historySize;
    private final int clientTimeoutMs;

    private ServerConfig(int port, boolean loggingEnabled, int historySize, int clientTimeoutMs) {
        this.port = port;
        this.loggingEnabled = loggingEnabled;
        this.historySize = historySize;
        this.clientTimeoutMs = clientTimeoutMs;
    }

    public static ServerConfig load(String[] args) {
        Properties properties = new Properties();
        loadFromClasspath(properties);
        if (args.length > 0) {
            loadFromFile(properties, Path.of(args[0]));
        }
        return new ServerConfig(
                Integer.parseInt(properties.getProperty("port", "5555")),
                Boolean.parseBoolean(properties.getProperty("logging.enabled", "true")),
                Integer.parseInt(properties.getProperty("history.size", "20")),
                Integer.parseInt(properties.getProperty("client.timeout.ms", "120000"))
        );
    }

    public int port() {
        return port;
    }

    public boolean loggingEnabled() {
        return loggingEnabled;
    }

    public int historySize() {
        return historySize;
    }

    public int clientTimeoutMs() {
        return clientTimeoutMs;
    }

    private static void loadFromClasspath(Properties properties) {
        try (InputStream input = ServerConfig.class.getClassLoader().getResourceAsStream("server.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException exception) {
            System.err.println("Cannot read default server.properties: " + exception.getMessage());
        }
    }

    private static void loadFromFile(Properties properties, Path path) {
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        } catch (IOException exception) {
            System.err.println("Cannot read config file " + path + ": " + exception.getMessage());
        }
    }
}
