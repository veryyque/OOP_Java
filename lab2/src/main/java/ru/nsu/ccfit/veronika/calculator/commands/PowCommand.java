// AddCommand.java
package ru.nsu.ccfit.veronika.calculator.commands;
import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;
import java.util.List;

public class PowCommand implements Command {
    public void execute(ExecContext context, List<String> args) throws CalculatorException {
        double b = context.pop();
        double a = context.pop();
        context.push(Math.pow(a,b));
    }
}