package ru.nsu.ccfit.veronika.calculator;

import ru.nsu.ccfit.veronika.calculator.commands.Command;
import ru.nsu.ccfit.veronika.calculator.context.ExecContext;
import ru.nsu.ccfit.veronika.calculator.exceptions.CalculatorException;
import ru.nsu.ccfit.veronika.calculator.factory.CommandFactory;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    static void main(String[] args) {
        logger.info("-------------Калькулятор запущен----------");

        ExecContext context = new ExecContext();
        CommandFactory factory = new CommandFactory();

        try (BufferedReader reader = getReader(args)) {
            String line;
            int lineNumber = 0; //для exception

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim(); //убрать пробелы в нач и конц строки

                if (line.isEmpty() || line.startsWith("#")) {
                    logger.info("Строка " + lineNumber + " пропущена");
                    continue;
                }

                String[] params = line.split("\\s+"); //сплит по любому пробельному символу
                String cmdName = params[0].toUpperCase();
                List<String> arguments = new ArrayList<>();   // сразу создать пустой список

                if (params.length > 1) {
                    for (int i = 1; i < params.length; i++) {
                        arguments.add(params[i]);
                    }
                }

                if ("EXIT".equals(cmdName)) {
                    logger.info("Получена команда завершения (" + cmdName + ")");
                    logger.info("------------Калькулятор завершил работу------------");
                    break;
                }

                try {
                    logger.info(String.format("Строка %d: %s %s", lineNumber, cmdName, arguments));
                    Command cmd = factory.create(cmdName);
                    cmd.execute(context, arguments);
                    logger.info("Команда " + cmdName + " выполнена успешно");
                } catch (CalculatorException e) {
                    String msg = String.format("Ошибка в строке %d: %s", lineNumber, e.getMessage());
                    logger.warning(msg);
                }
            }
        } catch (IOException e) {
            logger.severe("Ошибка чтения ввода: " + e.getMessage());
            throw new RuntimeException("Ошибка чтения файла", e);
        }

        logger.info("------------Калькулятор завершил работу------------");
    }

    private static BufferedReader getReader(String[] args) throws IOException {
        if (args.length > 0) {
            logger.info("Читаем команды из файла: " + args[0]);
            return new BufferedReader(new FileReader(args[0]));
        } else {
            logger.info("Читаем команды из консоли");
            return new BufferedReader(new InputStreamReader(System.in));
        }
    }
}