package ru.nsu.ccfit.veronika.calculator.context;

import ru.nsu.ccfit.veronika.calculator.exceptions.EmptyStackException;

import java.util.*;

public class ExecContext {
    private final Deque<Double> stack = new ArrayDeque<>();
    private final Map<String, Double> defines = new HashMap<>();

    public void push(double value) {
        stack.push(value);
    }

    public double pop() throws EmptyStackException {
        if (stack.isEmpty()) throw new EmptyStackException("Стек пуст");
        return stack.pop();
    }

    public double peek() throws EmptyStackException {
        if (stack.isEmpty()) throw new EmptyStackException("Стек пуст");
        return stack.peek();
    }

    public void define(String name, double value) {
        defines.put(name.toLowerCase(), value);
    }

    public Double getDefine(String name) {
        return defines.get(name.toLowerCase());
    }

    public void printTop() {
        try {
            System.out.println(peek());
        } catch (EmptyStackException e) {
            System.out.println("(стек пуст)");
        }
    }
}