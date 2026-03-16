package ru.nsu.ccfit.veronika.calculator.commands;

import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;
import ru.nsu.ccfit.veronika.calculator.exceptions.UnknownParameterException;

import java.util.List;

public class PushCommand implements Command {
    public void execute(ExecContext context, List<String> args) throws CalculatorException {
        if (args.isEmpty()) throw new CalculatorException("PUSH требует аргумент");

        String valueString = args.getFirst();
        double value;

        try {
            value = Double.parseDouble(valueString);
        } catch (NumberFormatException e) {
            Double def = context.getDefine(valueString);
            if (def == null) {
                throw new UnknownParameterException("Неизвестный параметр: " + valueString);
            }
            value = def;
        }
        context.push(value);
    }
}