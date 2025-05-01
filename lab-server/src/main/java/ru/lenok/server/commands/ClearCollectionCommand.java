package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.server.collection.LabWorkService;

import java.io.IOException;
import java.sql.SQLException;

import static ru.lenok.server.commands.CommandName.clear;
import static ru.lenok.server.commands.CommandName.show;


public class ClearCollectionCommand extends AbstractCommand {
    LabWorkService labWorkService;

    public ClearCollectionCommand(LabWorkService labWorkService) {
        super(clear.getBehavior(), "очистить коллекцию");
        this.labWorkService = labWorkService;
    }

    private CommandResponse execute(long ownerId) throws SQLException {
        labWorkService.clearCollection(ownerId);
        return new CommandResponse(EMPTY_RESULT);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException, SQLException {
        return execute(req.getUser().getId());
    }
}
