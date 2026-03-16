package ru.nsu.ccfit.veronika.calculator.commands;

import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;

import java.util.List;

public class SinCommand implements Command {
    public void execute(ExecContext context, List<String> args) throws CalculatorException {
        double x = context.pop();
        context.push(Math.sin(x));
    }
}