package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.server.collection.LabWorkService;

import java.io.IOException;
import java.sql.SQLException;

import static ru.lenok.server.collection.LabWorkService.sortMapAndStringify;
import static ru.lenok.server.commands.CommandName.show;

public class ShowCollectionCommand extends AbstractCommand {
    LabWorkService labWorkService;

    public ShowCollectionCommand(LabWorkService labWorkService) {
        super(show.getBehavior(), "вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
        this.labWorkService = labWorkService;
    }

    private CommandResponse execute() throws SQLException{
        String sortCollectionAndStringifyResult = sortMapAndStringify(labWorkService.getWholeMap());
        return new CommandResponse(labWorkService.getCollectionSize() == 0 ? "ПУСТАЯ КОЛЛЕКЦИЯ" : sortCollectionAndStringifyResult);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws Exception {
        return execute();
    }
}
