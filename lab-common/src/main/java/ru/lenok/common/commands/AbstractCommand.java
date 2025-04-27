package ru.lenok.common.commands;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class AbstractCommand implements Executable {
    protected static final String EMPTY_RESULT = "";
    private final CommandBehavior commandBehavior;
    private final String description;
}
