package ru.nsu.ccfit.veronika.calculator.commands;

import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;

import java.util.List;

public class DefineCommand implements Command {
    public void execute(ExecContext context, List<String> args) throws CalculatorException {
        if (args.size() < 2) throw new CalculatorException("DEFINE требует имя и значение");

        String name = args.get(0);
        try {
            double value = Double.parseDouble(args.get(1));
            context.define(name, value);
        } catch (NumberFormatException e) { //если не удалось преоразование строки в Double
            throw new CalculatorException("DEFINE: неверное число");
        }
    }
}