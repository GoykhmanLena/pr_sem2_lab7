package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.server.collection.LabWorkService;

import java.io.IOException;
import java.sql.SQLException;

import static ru.lenok.server.commands.CommandName.remove_key;

public class RemoveByKeyFromCollectionCommand extends AbstractCommand {
    LabWorkService labWorkService;

    public RemoveByKeyFromCollectionCommand(LabWorkService labWorkService) {
        super(remove_key.getBehavior(), "Аргумент - ключ. Удалить элемент из коллекции по его ключу");
        this.labWorkService = labWorkService;
    }

    private CommandResponse execute(String key) throws SQLException {
        labWorkService.remove(key);
        return new CommandResponse(EMPTY_RESULT);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException, SQLException {
        String key = req.getCommandWithArgument().getArgument1();
        labWorkService.checkAccess(req.getUser().getId(), key);
        return execute(key);
    }
}
