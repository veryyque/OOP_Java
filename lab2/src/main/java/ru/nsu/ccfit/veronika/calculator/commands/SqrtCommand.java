package ru.nsu.ccfit.veronika.calculator.commands;

import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;

import java.util.List;

public class SqrtCommand implements Command {
    public void execute(ExecContext context, List<String> args) throws CalculatorException {
        double x = context.pop();
        if (x < 0) throw new CalculatorException("SQRT: отрицательное число под корнем");
        context.push(Math.sqrt(x));
    }
}