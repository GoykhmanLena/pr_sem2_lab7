package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.server.collection.LabWorkService;

import java.io.IOException;
import java.sql.SQLException;

import static ru.lenok.server.commands.CommandName.print_ascending;


public class PrintAscendingCommand extends AbstractCommand {
    LabWorkService labWorkService;

    public PrintAscendingCommand(LabWorkService labWorkService) {
        super(print_ascending.getBehavior(), "вывести элементы коллекции в порядке возрастания");
        this.labWorkService = labWorkService;
    }
    private CommandResponse execute(){
        return new CommandResponse(labWorkService.getWholeMap());
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException {
        return execute();
    }
}
