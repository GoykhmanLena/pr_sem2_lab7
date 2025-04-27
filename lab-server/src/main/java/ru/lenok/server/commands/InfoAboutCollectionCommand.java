package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.collection.LabWorkService;

import java.io.IOException;

import static ru.lenok.server.commands.CommandName.info;

public class InfoAboutCollectionCommand extends AbstractCommand {
    LabWorkService labWorkService;

    public InfoAboutCollectionCommand(LabWorkService labWorkService) {
        super(info.getBehavior(), "вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)");
        this.labWorkService = labWorkService;
    }

    private CommandResponse execute() {
        return new CommandResponse("Это LabWorkCollection, текущий размер: " + labWorkService.getCollectionSize() + ", состоит из элементов типа: " + LabWork.class + "\n");
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException {
        return execute();
    }
}
