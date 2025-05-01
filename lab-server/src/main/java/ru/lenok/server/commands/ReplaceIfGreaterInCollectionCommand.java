package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.collection.LabWorkService;

import java.io.IOException;
import java.sql.SQLException;

import static ru.lenok.server.commands.CommandName.replace_if_greater;


public class ReplaceIfGreaterInCollectionCommand extends AbstractCommand {
    LabWorkService labWorkService;

    public ReplaceIfGreaterInCollectionCommand(LabWorkService labWorkService) {
        super(replace_if_greater.getBehavior(), "Аргумент - ключ. Элемент. Заменить значение по ключу, если новое значение больше старого");
        this.labWorkService = labWorkService;
    }

    private CommandResponse execute(String key, LabWork element) throws SQLException {
        labWorkService.replaceIfGreater(key, element);
        return new CommandResponse(EMPTY_RESULT);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException, SQLException {
        return execute(req.getCommandWithArgument().getArgument(), req.getElement());
    }
}
