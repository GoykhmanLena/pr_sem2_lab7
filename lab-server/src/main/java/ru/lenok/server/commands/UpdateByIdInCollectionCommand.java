package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.collection.LabWorkService;

import java.io.IOException;
import java.sql.SQLException;

import static ru.lenok.server.commands.CommandName.update_id;

public class UpdateByIdInCollectionCommand extends AbstractCommand {
    LabWorkService labWorkService;

    public UpdateByIdInCollectionCommand(LabWorkService labWorkService) {
        super(update_id.getBehavior(), "Аргумент - id. Элемент. Обновить значение элемента коллекции, id которого равен заданному");
        this.labWorkService = labWorkService;
    }

    private CommandResponse execute(String id_str, LabWork element) throws SQLException {
        Long id;
        try {
            id = Long.parseLong(id_str);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id имеет формат Long, попробуйте ввести еще раз");
        }
        labWorkService.updateByLabWorkId(id, element, true);
        return new CommandResponse(EMPTY_RESULT);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException, SQLException {
        return execute(req.getCommandWithArgument().getArgument1(), req.getElement());
    }
}