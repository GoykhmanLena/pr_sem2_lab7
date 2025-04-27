package ru.lenok.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.lenok.common.commands.CommandBehavior;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class CommandWithArgument implements Serializable {
    private final String commandName;
    private final CommandBehavior commandBehavior;
    private final String argument;
}