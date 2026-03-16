package ru.nsu.ccfit.veronika.calculator.factory;

import ru.nsu.ccfit.veronika.calculator.commands.Command;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;
import ru.nsu.ccfit.veronika.calculator.exceptions.UnknownCommandException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CommandFactory {
    private final Properties dictionary = new Properties();

    public CommandFactory() {
        loadDictionary();
    }

    private void loadDictionary() {
        try (InputStream inputStream = CommandFactory.class.getResourceAsStream("/commands.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Файл commands.properties не найден в resources!");
            }
            dictionary.load(inputStream); //пара ADD - classpath
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить commands.properties", e);
        }
    }

    public Command create(String commandName) throws CalculatorException {
        String className = dictionary.getProperty(commandName.toUpperCase());

        if (className == null) {
            throw new UnknownCommandException("Неизвестная команда: " + commandName);
        }

        try {
            Class<?> clazz = Class.forName(className);
            return (Command) clazz.getDeclaredConstructor().newInstance();

        } catch (ReflectiveOperationException e) {
            throw new CalculatorException("Не удалось создать команду '" + commandName + "': " + e.getMessage());
        }
    }
}