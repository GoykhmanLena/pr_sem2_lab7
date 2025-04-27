package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.server.collection.LabWorkService;

import java.io.IOException;

import static ru.lenok.server.commands.CommandName.remove_key;

public class RemoveByKeyFromCollectionCommand extends AbstractCommand {
    LabWorkService labWorkService;

    public RemoveByKeyFromCollectionCommand(LabWorkService labWorkService) {
        super(remove_key.getBehavior(), "Аргумент - ключ. Удалить элемент из коллекции по его ключу");
        this.labWorkService = labWorkService;
    }

    private CommandResponse execute(String key) {
        labWorkService.remove(key);
        return new CommandResponse(EMPTY_RESULT);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException {
        return execute(req.getCommandWithArgument().getArgument());
    }
}
