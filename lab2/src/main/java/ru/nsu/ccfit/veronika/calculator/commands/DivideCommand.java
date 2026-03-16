// DivideCommand.java
package ru.nsu.ccfit.veronika.calculator.commands;
import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;
import ru.nsu.ccfit.veronika.calculator.exceptions.DivisionByZeroException;
import java.util.List;

public class DivideCommand implements Command {
    public void execute(ExecContext context, List<String> args) throws CalculatorException {
        double b = context.pop();
        double a = context.pop();
        if (b == 0 | b == 0.0) throw new DivisionByZeroException("Деление на ноль");
        context.push(a / b);
    }
}