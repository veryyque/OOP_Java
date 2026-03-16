package ru.nsu.ccfit.veronika.calculator.factory;

import org.junit.jupiter.api.Test;
import ru.nsu.ccfit.veronika.calculator.commands.Command;
import ru.nsu.ccfit.veronika.calculator.commands.PushCommand;
import ru.nsu.ccfit.veronika.calculator.commands.PrintCommand;
import ru.nsu.ccfit.veronika.calculator.exceptions.UnknownCommandException;

import static org.junit.jupiter.api.Assertions.*;

class CommandFactoryTest {

    @Test
    void create_push_works() throws Exception {
        CommandFactory factory = new CommandFactory();

        Command cmd = factory.create("PUSH");

        assertNotNull(cmd, "Команда не должна быть null");
        assertTrue(cmd instanceof PushCommand, "Должен вернуться PushCommand");
    }

    @Test
    void create_print_works() throws Exception {
        CommandFactory factory = new CommandFactory();

        Command cmd = factory.create("PRINT");

        assertNotNull(cmd);
        assertTrue(cmd instanceof PrintCommand);
    }

    @Test
    void create_unknown_command() {
        CommandFactory factory = new CommandFactory();

        UnknownCommandException ex = assertThrows(UnknownCommandException.class, () -> {
            factory.create("НЕИЗВЕСТНАЯ");
        });

        assertTrue(ex.getMessage().contains("Неизвестная команда"));
    }

    @Test
    void create_lovercase() throws Exception {
        CommandFactory factory = new CommandFactory();

        Command cmd = factory.create("push");  // должно привести к верхнему регистру внутри

        assertNotNull(cmd);
        assertTrue(cmd instanceof PushCommand);
    }
}