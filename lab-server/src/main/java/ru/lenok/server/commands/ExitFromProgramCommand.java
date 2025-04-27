package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;

import java.io.IOException;

import static ru.lenok.server.commands.CommandName.exit;

public class ExitFromProgramCommand extends AbstractCommand {
    public ExitFromProgramCommand() {
        super(exit.getBehavior(), "завершить программу (без сохранения в файл)");
    }

    private CommandResponse execute() {
        return new CommandResponse(EMPTY_RESULT);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException {
        return execute();
    }
}
