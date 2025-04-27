package ru.lenok.client.client_command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.common.commands.CommandBehavior;

import java.io.IOException;

public class ExitFromProgramCommand extends AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(ExitFromProgramCommand.class);
    public ExitFromProgramCommand(CommandBehavior behavior) {
        super(behavior, "завершить программу клиента (без сохранения в файл)");
    }

    public CommandResponse execute() {
        logger.info("Завершаю программу клиента");
        System.exit(0);
        return new CommandResponse(EMPTY_RESULT);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException {
        return execute();
    }
}
