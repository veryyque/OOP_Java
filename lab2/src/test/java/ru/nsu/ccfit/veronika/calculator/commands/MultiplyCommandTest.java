package ru.nsu.ccfit.veronika.calculator.commands;

import org.junit.jupiter.api.Test;
import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;
import ru.nsu.ccfit.veronika.calculator.exceptions.EmptyStackException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiplyCommandTest {

    @Test
    void multiply_works() throws CalculatorException {
        ExecContext context = new ExecContext();
        context.push(3.0);   // первое число
        context.push(4.0);   // второе число

        MultiplyCommand cmd = new MultiplyCommand();
        cmd.execute(context, List.of());

        assertEquals(12.0, context.pop(), "3 * 4 должно быть 12");
    }

    @Test
    void multiply_negative() throws CalculatorException {
        ExecContext context = new ExecContext();
        context.push(-5.0);
        context.push(2.0);

        MultiplyCommand cmd = new MultiplyCommand();
        cmd.execute(context, List.of());

        assertEquals(-10.0, context.pop(), "-5 * 2 = -10");
    }

    @Test
    void multiply_one_arg() {
        ExecContext context = new ExecContext();
        context.push(7.0);  // только одно число

        MultiplyCommand cmd = new MultiplyCommand();

        EmptyStackException ex = assertThrows(EmptyStackException.class, () -> {
            cmd.execute(context, List.of());
        });

        assertEquals("Стек пуст", ex.getMessage(), "Должно быть исключение — не хватает второго числа");
    }

    @Test
    void multiply_empty_stack() {
        ExecContext context = new ExecContext();

        MultiplyCommand cmd = new MultiplyCommand();

        EmptyStackException ex = assertThrows(EmptyStackException.class, () -> {
            cmd.execute(context, List.of());
        });

        assertEquals("Стек пуст", ex.getMessage());
    }
}