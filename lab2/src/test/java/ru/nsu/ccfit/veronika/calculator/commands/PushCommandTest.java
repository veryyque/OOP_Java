package ru.nsu.ccfit.veronika.calculator.commands;

import org.junit.jupiter.api.Test;
import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;
import ru.nsu.ccfit.veronika.calculator.exceptions.UnknownParameterException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PushCommandTest {

    @Test
    void push_number() throws CalculatorException {
        ExecContext context = new ExecContext();
        PushCommand cmd = new PushCommand();

        cmd.execute(context, List.of("42.0"));

        assertEquals(42.0, context.pop());
    }

    @Test
    void push_variable() throws CalculatorException {
        ExecContext context = new ExecContext();
        context.define("x", 777.0);

        PushCommand cmd = new PushCommand();
        cmd.execute(context, List.of("x"));

        assertEquals(777.0, context.pop());
    }

    @Test
    void push_unknown_parameter() {
        ExecContext context = new ExecContext();
        PushCommand cmd = new PushCommand();

        UnknownParameterException ex = assertThrows(UnknownParameterException.class, () -> {
            cmd.execute(context, List.of("y"));
        });

        assertTrue(ex.getMessage().contains("Неизвестный параметр"));
    }

    @Test
    void push_no_args() {
        ExecContext context = new ExecContext();
        PushCommand cmd = new PushCommand();

        CalculatorException ex = assertThrows(CalculatorException.class, () -> {
            cmd.execute(context, List.of());
        });

        assertEquals("PUSH требует аргумент", ex.getMessage());
    }
}