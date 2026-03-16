package ru.nsu.ccfit.veronika.calculator.commands;

import org.junit.jupiter.api.Test;
import ru.nsu.ccfit.veronika.calculator.context.ExecContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrintCommandTest {

    @Test
    void print_works() throws Exception {
        ExecContext context = new ExecContext();
        context.push(456.78);

        PrintCommand cmd = new PrintCommand();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(out));

        cmd.execute(context, List.of());

        String result = out.toString().trim();
        assertEquals("456.78", result);

        System.setOut(oldOut);
    }

    @Test
    void print_empty_stack() throws Exception {
        ExecContext context = new ExecContext();

        PrintCommand cmd = new PrintCommand();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(out));

        cmd.execute(context, List.of());

        String result = out.toString().trim();
        assertEquals("(стек пуст)", result);

        System.setOut(oldOut);
    }
}