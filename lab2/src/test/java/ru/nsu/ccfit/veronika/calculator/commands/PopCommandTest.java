package ru.nsu.ccfit.veronika.calculator.commands;

import org.junit.jupiter.api.Test;
import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;
import ru.nsu.ccfit.veronika.calculator.exceptions.EmptyStackException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PopCommandTest {

    @Test
    void pop_works() throws CalculatorException {
        ExecContext context = new ExecContext();
        context.push(1.0);
        context.push(2.0);

        PopCommand cmd = new PopCommand();
        cmd.execute(context, List.of());

        assertEquals(1.0, context.pop());  // остался только 1
    }

    @Test
    void pop_empty_sta() {
        ExecContext context = new ExecContext();
        PopCommand cmd = new PopCommand();

        EmptyStackException ex = assertThrows(EmptyStackException.class, () -> {
            cmd.execute(context, List.of());
        });

        assertEquals("Стек пуст", ex.getMessage());
    }
}