package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;

import java.io.IOException;
import java.util.stream.Collectors;

import static ru.lenok.server.commands.CommandName.help;


public class HelpCommand extends AbstractCommand {
    private final CommandRegistry commandRegistry;

    public HelpCommand(CommandRegistry inv) {
        super(help.getBehavior(), "вывести справку по доступным командам");
        this.commandRegistry = inv;
    }

    private CommandResponse execute() {
        String result = commandRegistry.getClientCommandDefinitions().entrySet().stream()
                .map(entry -> commandRegistry.getCommandDescription(entry.getKey()))
                .collect(Collectors.joining("\n"));
        return new CommandResponse(result);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException {
        return execute();
    }
}
