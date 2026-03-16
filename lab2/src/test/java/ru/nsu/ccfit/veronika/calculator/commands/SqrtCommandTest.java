package ru.nsu.ccfit.veronika.calculator.commands;

import org.junit.jupiter.api.Test;
import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqrtCommandTest {

    @Test
    void sqrt_works() throws CalculatorException {
        ExecContext context = new ExecContext();
        context.push(16.0);

        SqrtCommand cmd = new SqrtCommand();
        cmd.execute(context, List.of());

        assertEquals(4.0, context.pop(), "sqrt(16) = 4");
    }

    @Test
    void sqrt_negative() {
        ExecContext context = new ExecContext();
        context.push(-9.0);

        SqrtCommand cmd = new SqrtCommand();

        CalculatorException ex = assertThrows(CalculatorException.class, () -> {
            cmd.execute(context, List.of());
        });

        assertTrue(ex.getMessage().contains("отрицательное число"));
    }

    @Test
    void sqrt_empty_stack() {
        ExecContext context = new ExecContext();

        SqrtCommand cmd = new SqrtCommand();

        Exception ex = assertThrows(Exception.class, () -> {
            cmd.execute(context, List.of());
        });

        assertTrue(ex.getMessage().contains("Стек пуст"));
    }
}