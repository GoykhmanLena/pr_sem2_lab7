package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.collection.LabWorkService;

import java.io.IOException;

import static ru.lenok.server.commands.CommandName.remove_greater;

public class RemoveGreaterFromCollectionCommand extends AbstractCommand {
    LabWorkService labWorkService;

    public RemoveGreaterFromCollectionCommand(LabWorkService labWorkService) {
        super(remove_greater.getBehavior(), "Элемент. Удалить из коллекции все элементы, превышающие заданный");
        this.labWorkService = labWorkService;
    }

    private CommandResponse execute(LabWork element) {
        labWorkService.removeGreater(element);
        return new CommandResponse(EMPTY_RESULT);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException {
        return execute(req.getElement());
    }
}
