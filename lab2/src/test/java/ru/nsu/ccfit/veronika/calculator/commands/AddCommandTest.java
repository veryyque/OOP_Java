package ru.nsu.ccfit.veronika.calculator.commands;

import org.junit.jupiter.api.Test;
import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;
import ru.nsu.ccfit.veronika.calculator.exceptions.EmptyStackException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AddCommandTest {

    @Test
    void add_works() throws CalculatorException {
        ExecContext context = new ExecContext();
        context.push(5.0);
        context.push(7.0);

        AddCommand cmd = new AddCommand();
        cmd.execute(context, List.of());

        assertEquals(12.0, context.pop());
    }

    @Test
    void add_not_enough_args() throws CalculatorException {
        ExecContext context = new ExecContext();
        context.push(10.0);

        AddCommand cmd = new AddCommand();

        EmptyStackException ex = assertThrows(EmptyStackException.class, () -> {
            cmd.execute(context, List.of());
        });

        assertEquals("Стек пуст", ex.getMessage());
    }
}