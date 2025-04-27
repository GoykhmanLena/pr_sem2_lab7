package ru.lenok.server.request_processing;

import lombok.AllArgsConstructor;
import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.CommandWithArgument;
import ru.lenok.common.commands.Executable;
import ru.lenok.server.commands.CommandName;
import ru.lenok.server.commands.CommandRegistry;

@AllArgsConstructor
public class CommandController {
    private final CommandRegistry commandRegistry;
    public CommandResponse handle(CommandRequest request) {
        CommandWithArgument commandWithArgument = request.getCommandWithArgument();
        CommandName commandName = CommandName.valueOf(commandWithArgument.getCommandName());
        CommandResponse executionResult;
        try {
            Executable command = commandRegistry.getCommand(commandName);
            executionResult = command.execute(request);
        } catch (Exception e) {
            executionResult = new CommandResponse(e);
        }
        return executionResult;
    }
}
