package ru.lenok.server.request_processing;

import lombok.AllArgsConstructor;
import org.postgresql.util.PSQLException;
import ru.lenok.common.auth.LoginResponse;
import ru.lenok.common.auth.User;
import ru.lenok.common.commands.CommandBehavior;
import ru.lenok.server.services.UserService;

import java.sql.SQLException;
import java.util.Map;

@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final Map<String, CommandBehavior> clientCommandDefinitions;
    public LoginResponse register(User user){
        try {
            User userFromDb = userService.register(user);
            return new LoginResponse(null, clientCommandDefinitions, userFromDb.getId());
        } catch (PSQLException e){
            return new LoginResponse(new IllegalArgumentException("Пользователь с таким логином уже существует, выберите другой"), null, -1);
        } catch (Exception e){
            return new LoginResponse(e, null, -1);
        }
    }

    public LoginResponse login(User user){
        try {
            User userFromDb = userService.login(user);
            return new LoginResponse(null, clientCommandDefinitions, userFromDb.getId());
        } catch (Exception e){
            return new LoginResponse(e, null, -1);
        }
    }
}
