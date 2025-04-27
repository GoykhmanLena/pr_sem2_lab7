package ru.lenok.server.commands;


import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.collection.LabWorkService;
import ru.lenok.server.utils.IdCounterService;

import java.io.IOException;

import static ru.lenok.server.commands.CommandName.insert;


public class InsertToCollectionCommand extends AbstractCommand {
    LabWorkService labWorkService;


    public InsertToCollectionCommand(LabWorkService labWorkService) {
        super(insert.getBehavior(), "Аргумент - ключ; Элемент; Добавить новый элемент с заданным ключом");
        this.labWorkService = labWorkService;
    }

    private CommandResponse execute(String key, LabWork element) {
        element.setId(IdCounterService.getNextId());
        String warning = labWorkService.put(key, element);
        return new CommandResponse (warning == null ? EMPTY_RESULT : warning);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException {
        return execute(req.getCommandWithArgument().getArgument(), req.getElement());
    }
}
