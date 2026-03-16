package ru.nsu.ccfit.veronika.calculator.commands;

import org.junit.jupiter.api.Test;
import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;
import ru.nsu.ccfit.veronika.calculator.exceptions.EmptyStackException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DivideCommandTest {

    @Test
    void divide_works() throws CalculatorException {
        ExecContext context = new ExecContext();
        context.push(10.0);
        context.push(2.0);

        DivideCommand cmd = new DivideCommand();
        cmd.execute(context, List.of());

        assertEquals(5.0, context.pop(), "10 / 2 = 5");
    }
    @Test
    void divide_not_enough_args() {
        ExecContext context = new ExecContext();
        context.push(8.0);

        DivideCommand cmd = new DivideCommand();

        EmptyStackException ex = assertThrows(EmptyStackException.class, () -> {
            cmd.execute(context, List.of());
        });

        assertEquals("Стек пуст", ex.getMessage());
    }
}