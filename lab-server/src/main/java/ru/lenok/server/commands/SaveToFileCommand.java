package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.server.collection.LabWorkService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static ru.lenok.server.commands.CommandName.save;

public class SaveToFileCommand extends AbstractCommand {
    LabWorkService labWorkService;

    public SaveToFileCommand(LabWorkService labWorkService) {
        super(save.getBehavior(), "сохранить коллекцию в файл");
        this.labWorkService = labWorkService;
    }

    private CommandResponse execute() throws IOException {
        String json = labWorkService.getCollectionAsJson();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(labWorkService.getFileName()))) {
            writer.write(json);
        }
        return new CommandResponse(EMPTY_RESULT);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException {
        return execute();
    }
}
