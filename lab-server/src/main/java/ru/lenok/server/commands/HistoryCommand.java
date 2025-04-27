package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;

import java.io.IOException;

import static ru.lenok.server.commands.CommandName.history;

public class HistoryCommand extends AbstractCommand {
    private final IHistoryProvider historyProvider;

    public HistoryCommand(IHistoryProvider historyProvider) {
        super(history.getBehavior(), "вывести последние 15 команд (без их аргументов)");
        this.historyProvider = historyProvider;
    }

    private CommandResponse execute(String clientID) {
        String lastNCommands = historyProvider.getHistoryByClientID(clientID).getLastNCommands(15);
        return new CommandResponse("История клиента с ID: " + clientID + "\n" + lastNCommands);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException {
        return execute(req.getClientID().toString());
    }
}
