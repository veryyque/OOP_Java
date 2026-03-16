package ru.nsu.ccfit.veronika.calculator.context;

import org.junit.jupiter.api.Test;
import ru.nsu.ccfit.veronika.calculator.exceptions.EmptyStackException;

import static org.junit.jupiter.api.Assertions.*;

class ExecContextTest {

    @Test
    void push_and_pop_works() {
        ExecContext context = new ExecContext();

        context.push(3.5);
        context.push(10.0);

        assertEquals(10.0, context.pop(), "Верхний элемент должен быть 10");
        assertEquals(3.5, context.pop(), "Следующий элемент должен быть 3.5");
    }

    @Test
    void pop_empty_stack_exception() {
        ExecContext context = new ExecContext();

        EmptyStackException ex = assertThrows(EmptyStackException.class, context::pop);

        assertEquals("Стек пуст", ex.getMessage());
    }

    @Test
    void peek_empty_stack_exception() {
        ExecContext context = new ExecContext();

        EmptyStackException ex = assertThrows(EmptyStackException.class, context::peek);

        assertEquals("Стек пуст", ex.getMessage());
    }

    @Test
    void define_getDefine_works() {
        ExecContext context = new ExecContext();

        context.define("pi", 3.14159);
        context.define("e", 2.718);

        assertEquals(3.14159, context.getDefine("pi"));
        assertEquals(2.718, context.getDefine("e"));
        assertNull(context.getDefine("нет_такой"), "Неизвестная переменная должна вернуть null");
    }
}