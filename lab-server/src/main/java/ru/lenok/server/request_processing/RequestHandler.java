package ru.lenok.server.request_processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.CommandWithArgument;
import ru.lenok.common.commands.CommandBehavior;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.commands.CommandName;
import ru.lenok.server.commands.CommandRegistry;
import ru.lenok.server.commands.IHistoryProvider;
import ru.lenok.server.utils.HistoryList;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static ru.lenok.common.commands.ArgType.LONG;
import static ru.lenok.server.commands.CommandName.exit;
import static ru.lenok.server.commands.CommandName.save;

public class RequestHandler implements IHistoryProvider {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private final CommandController commandController;
    private final CommandRegistry commandRegistry;
    private Map<String, HistoryList> historyByClients = new ConcurrentHashMap();

    public CommandController getCommandController() {
        return commandController;
    }

    public RequestHandler(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
        this.commandController = new CommandController(commandRegistry);
    }

    public Object onReceive(Object inputData) {
        if (inputData instanceof CommandRequest) {
            CommandRequest commandRequest = (CommandRequest) inputData;
            CommandResponse validateResponse = validateCommandRequest(commandRequest);
            if (validateResponse != null){
                return validateResponse;
            }
            String commandNameStr = commandRequest.getCommandWithArgument().getCommandName();
            CommandName commandName = CommandName.valueOf(commandNameStr);
            UUID clientID = commandRequest.getClientID();
            HistoryList historyList = historyByClients.get(clientID.toString());
            if (historyList == null) {
                logger.warn("клиент с таким id не зарегистрирован, регистрирую " + clientID);
                historyList = new HistoryList();
                historyByClients.put(clientID.toString(), historyList);
            }
            historyList.addCommand(commandName);
            return commandController.handle(commandRequest);
        } else if (inputData instanceof UUID) {
            historyByClients.put(inputData.toString(), new HistoryList());
            return commandRegistry.getClientCommandDefinitions();
        }
        return errorResponse("Вы передали какую-то чепуху: ", inputData);
    }

    private static CommandResponse errorResponse(String message, Object inputData) {
        return new CommandResponse(new IllegalArgumentException(message + inputData));
    }

    @Override
    public HistoryList getHistoryByClientID(String clientID) {
        return historyByClients.get(clientID);
    }

    private CommandResponse validateCommandRequest(CommandRequest commandRequest) {
        LabWork element = commandRequest.getElement();
        CommandWithArgument commandWithArgument = commandRequest.getCommandWithArgument();
        if (commandWithArgument == null){
            return errorResponse("Неверный формат запроса: ", commandRequest);
        }
        String commandNameStr = commandWithArgument.getCommandName();
        if (commandNameStr == null){
            return errorResponse("Неверный формат запроса: ", commandRequest);
        }
        CommandName commandName;
        try {
            commandName = CommandName.valueOf(commandNameStr);
        }
        catch (IllegalArgumentException e){
            return errorResponse("Такой команды не существует: ", commandRequest);
        }
        if (commandName == save || commandName == exit){
            return errorResponse("Вы МОШЕННИК: эта команда на сервере не разрешена: ", commandRequest);
        }
        CommandBehavior commandBehavior = commandName.getBehavior();
        String argument = commandWithArgument.getArgument();
        if (commandBehavior.hasArg()) {
            if (argument == null || argument.isEmpty()) {
                return errorResponse("Ожидался аргумент, ничего не пришло: ", commandRequest);
            }
            if (commandBehavior.getArgType() == LONG){
                try {
                    Long.parseLong(argument);
                } catch (NumberFormatException e){
                    return errorResponse("Ожидался аргумент типа Long, пришло: ", commandRequest);
                }
            }
        }
        if (commandBehavior.hasElement()) {
            if (element == null) {
                return errorResponse("Ожидался элемент, ничего не пришло: ", commandRequest);
            }
            if (!element.validate()) {
                return errorResponse("Вы передали невалидный элемент: ", commandRequest);
            }
        }
        return null;
    }
}
