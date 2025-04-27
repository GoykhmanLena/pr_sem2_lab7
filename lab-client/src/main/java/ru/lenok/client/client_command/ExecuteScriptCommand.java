package ru.lenok.client.client_command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.client.ClientInputProcessor;
import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.AbstractCommand;
import ru.lenok.client.input.AbstractInput;
import ru.lenok.client.input.FileInput;
import ru.lenok.common.commands.CommandBehavior;

import java.io.File;
import java.io.IOException;



public class ExecuteScriptCommand extends AbstractCommand {
    private final ClientInputProcessor inputProcessor;
    private static final Logger logger = LoggerFactory.getLogger(ExecuteScriptCommand.class);

    public ExecuteScriptCommand(ClientInputProcessor inpPr, CommandBehavior behavior) {
        super(behavior, "Аргумент - filename. Считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.");
        this.inputProcessor = inpPr;
    }

    public CommandResponse execute(String arg) throws IOException {
        File file = new File(arg);
        logger.info("-------------------- Начало выполнения файла: " + file.getCanonicalPath() + " ---------------------------------------------------------------------");
        if (inputProcessor.checkContext(file.getCanonicalPath())) {
            throw new IllegalArgumentException("Обнаружен ЦИКЛ, файл: " + file + " не будет открыт");
        }
        inputProcessor.setScriptExecutionContext(file.getCanonicalPath());
        try (AbstractInput fileInput = new FileInput(file)) {
            inputProcessor.processInput(fileInput, false);
        } catch (IOException e) {
            throw new IOException("Ошибка при чтении файла, проверьте что он существует");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Произошла ошибка,принудительное завершение чтения файла", e);
        } finally {
            inputProcessor.exitContext();
            logger.info("-------------------- Конец выполнения файла: " + file.getCanonicalPath() + " ---------------------------------------------------------------------");
        }
        return new CommandResponse(EMPTY_RESULT);
    }

    @Override
    public CommandResponse execute(CommandRequest req) throws IOException {
        return execute(req.getCommandWithArgument().getArgument());
    }
}
