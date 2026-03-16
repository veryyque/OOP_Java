package ru.nsu.ccfit.veronika.calculator.commands;

import org.junit.jupiter.api.Test;
import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefineCommandTest {

    @Test
    void define_variable() throws CalculatorException {
        ExecContext context = new ExecContext();
        DefineCommand cmd = new DefineCommand();

        cmd.execute(context, List.of("test", "999.5"));

        assertEquals(999.5, context.getDefine("test"));
    }

    @Test
    void define_no_args() {
        ExecContext context = new ExecContext();
        DefineCommand cmd = new DefineCommand();

        CalculatorException ex = assertThrows(CalculatorException.class, () -> {
            cmd.execute(context, List.of());
        });

        assertTrue(ex.getMessage().contains("требует"));
    }
}