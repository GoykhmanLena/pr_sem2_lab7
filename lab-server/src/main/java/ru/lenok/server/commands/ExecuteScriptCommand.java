package ru.lenok.server.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.server.collection.LabWorkService;

import java.io.IOException;

import static ru.lenok.server.commands.CommandName.execute_script;


public class ExecuteScriptCommand extends AbstractCommand {

    public ExecuteScriptCommand(LabWorkService labWorkService) {
        super(execute_script.getBehavior(), "Аргумент - filename, считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.");
    }

    private CommandResponse execute() throws IOException {
        return new CommandResponse("execute_script добавлен в историю");
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException {
        return execute();
    }
}
