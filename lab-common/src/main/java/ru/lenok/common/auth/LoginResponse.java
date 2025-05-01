package ru.lenok.common.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.lenok.common.commands.CommandBehavior;

import java.io.Serializable;
import java.util.Map;

@AllArgsConstructor
@Data
public class LoginResponse implements Serializable{
    private final Exception error;
    private final Map<String, CommandBehavior> clientCommandDefinitions;
    private final long userId;
}
