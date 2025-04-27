package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.server.collection.LabWorkService;
import ru.lenok.server.utils.IdCounterService;

import java.io.IOException;

import static ru.lenok.server.commands.CommandName.clear;


public class ClearCollectionCommand extends AbstractCommand {
    LabWorkService labWorkService;

    public ClearCollectionCommand(LabWorkService labWorkService) {
        super(clear.getBehavior(), "очистить коллекцию");
        this.labWorkService = labWorkService;
    }

    private CommandResponse execute() {
        labWorkService.clear_collection();
        IdCounterService.setId(0);
        return new CommandResponse(EMPTY_RESULT);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException {
        return execute();
    }
}
