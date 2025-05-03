package ru.lenok.server.request_processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.CommandWithArgument;
import ru.lenok.common.auth.LoginRequest;
import ru.lenok.common.auth.LoginResponse;
import ru.lenok.common.auth.User;
import ru.lenok.common.commands.CommandBehavior;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.commands.CommandName;
import ru.lenok.server.commands.CommandRegistry;
import ru.lenok.server.commands.IHistoryProvider;
import ru.lenok.server.connectivity.IncomingMessage;
import ru.lenok.server.connectivity.ResponseWithClient;
import ru.lenok.server.services.UserService;
import ru.lenok.server.utils.HistoryList;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import static ru.lenok.common.commands.ArgType.LONG;
import static ru.lenok.server.commands.CommandName.exit;
import static ru.lenok.server.commands.CommandName.save;

public class RequestHandler implements IHistoryProvider, Runnable {
    private static final int THREAD_COUNT = 5;
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private final CommandController commandController;
    private final CommandRegistry commandRegistry;
    private final UserController userController;
    private Map<Long, HistoryList> historyByClients = new ConcurrentHashMap();
    private final UserService userService;
    private final BlockingQueue<ResponseWithClient> responseQueue;
    private final BlockingQueue<IncomingMessage> incomingMessageQueue;
    ForkJoinPool pool;

    public CommandController getCommandController() {
        return commandController;
    }

    public RequestHandler(CommandRegistry commandRegistry, UserService userService, BlockingQueue<ResponseWithClient> responseQueue, BlockingQueue<IncomingMessage> incomingMessageQueue) {
        this.userService = userService;
        this.commandRegistry = commandRegistry;
        this.commandController = new CommandController(commandRegistry);
        this.userController = new UserController(userService, commandRegistry.getClientCommandDefinitions());
        this.responseQueue = responseQueue;
        this.incomingMessageQueue = incomingMessageQueue;
    }
    public void run(){
        logger.info("Запущен RequestHandler");
        int parallelism = Runtime.getRuntime().availableProcessors();
        pool = new ForkJoinPool(parallelism);

        while (true){
            try {
                IncomingMessage message = incomingMessageQueue.take();
                pool.execute(() -> {
                    Object response = onReceive(message.getMessage());
                    responseQueue.add(new ResponseWithClient(response, message.getClientIp(), message.getClientPort()));
                });
            } catch (InterruptedException e) {
                pool.shutdown();
                Thread.currentThread().interrupt();
            }
        }
    }

    public Object onReceive(Object inputData){
        logger.info("Обрабатываю сообщение: " + inputData);
        if (inputData instanceof CommandRequest) {
            CommandRequest commandRequest = (CommandRequest) inputData;
            CommandResponse validateResponse = validateCommandRequest(commandRequest);
            if (validateResponse != null){
                return validateResponse;
            }
            String commandNameStr = commandRequest.getCommandWithArgument().getCommandName();
            CommandName commandName = CommandName.valueOf(commandNameStr);
            User user = commandRequest.getUser();
            User userFromDb;
            try {
                userFromDb = userService.login(user);
                commandRequest.setUser(userFromDb);
            } catch (Exception e){
                return new LoginResponse(e, null, -1);
            }
            HistoryList historyList = historyByClients.get(userFromDb.getId());
            if (historyList == null) {
                historyList = new HistoryList();
                historyByClients.put(userFromDb.getId(), historyList);
            }
            historyList.addCommand(commandName);
            return commandController.handle(commandRequest);
        } else if (inputData instanceof LoginRequest) {
            LoginRequest loginRequest = (LoginRequest) inputData;
            LoginResponse loginResponse = null;
            if (loginRequest.isRegister()){
                loginResponse = userController.register(loginRequest.getUser());
            }
            else{
                loginResponse = userController.login(loginRequest.getUser());
            }
            if (loginResponse.getError() == null){
                historyByClients.put(loginResponse.getUserId(), new HistoryList());
            }
            return loginResponse;
        }
        return errorResponse("Вы передали какую-то чепуху: ", inputData);    }

    private static CommandResponse errorResponse(String message, Object inputData) {
        return new CommandResponse(new IllegalArgumentException(message + inputData));
    }

    @Override
    public HistoryList getHistoryByClientID(Long clientID) {
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
