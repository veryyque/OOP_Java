package ru.nsu.ccfit.veronika.calculator.commands;

import org.junit.jupiter.api.Test;
import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;
import ru.nsu.ccfit.veronika.calculator.exceptions.EmptyStackException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubtractCommandTest {

    @Test
    void subtract_works() throws CalculatorException {
        ExecContext context = new ExecContext();
        context.push(10.0);   // уменьшаемое
        context.push(3.0);    // вычитаемое

        SubtractCommand cmd = new SubtractCommand();
        cmd.execute(context, List.of());

        assertEquals(7.0, context.pop(), "10 - 3 = 7");
    }

    @Test
    void subtract_negative() throws CalculatorException {
        ExecContext context = new ExecContext();
        context.push(4.0);
        context.push(9.0);

        SubtractCommand cmd = new SubtractCommand();
        cmd.execute(context, List.of());

        assertEquals(-5.0, context.pop(), "4 - 9 = -5");
    }

    @Test
    void subtract_one_arg() {
        ExecContext context = new ExecContext();
        context.push(20.0);  // только одно число

        SubtractCommand cmd = new SubtractCommand();

        EmptyStackException ex = assertThrows(EmptyStackException.class, () -> {
            cmd.execute(context, List.of());
        });

        assertEquals("Стек пуст", ex.getMessage());
    }

    @Test
    void subtract_empty_stack() {
        ExecContext context = new ExecContext();

        SubtractCommand cmd = new SubtractCommand();

        EmptyStackException ex = assertThrows(EmptyStackException.class, () -> {
            cmd.execute(context, List.of());
        });

        assertEquals("Стек пуст", ex.getMessage());
    }
}