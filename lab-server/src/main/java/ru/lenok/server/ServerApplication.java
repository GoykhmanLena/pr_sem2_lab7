package ru.lenok.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandWithArgument;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.collection.LabWorkService;
import ru.lenok.server.commands.CommandRegistry;
import ru.lenok.server.commands.IHistoryProvider;
import ru.lenok.server.connectivity.IncomingMessage;
import ru.lenok.server.connectivity.ServerConnectionListener;
import ru.lenok.server.connectivity.ServerResponseSender;
import ru.lenok.server.request_processing.RequestHandler;
import ru.lenok.server.utils.HistoryList;
import ru.lenok.server.utils.IdCounterService;
import ru.lenok.server.utils.JsonReader;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.Hashtable;

import static java.lang.Math.max;
import static ru.lenok.server.commands.CommandName.save;

public class ServerApplication implements IHistoryProvider {
    private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);
    private final String filename;
    private LabWorkService labWorkService;
    private CommandRegistry commandRegistry;
    private RequestHandler requestHandler;
    private final int port;
    private ServerConnectionListener serverConnectionListener;
    private ServerResponseSender serverResponseSender;

    public ServerApplication(String file, int port) {
        this.filename = file;
        this.port = port;
        init();
    }

    public void start() {
        logger.info("Сервер работает");
        while (true) {
            try {
                IncomingMessage incomingMessage = serverConnectionListener.listenAndReceiveMessage();
                Object responseToBeSent = requestHandler.onReceive(incomingMessage.getMessage());
                serverResponseSender.sendMessageToClient(responseToBeSent, incomingMessage.getClientIp(), incomingMessage.getClientPort());
            } catch (Exception e) {
                logger.error("Ошибка, ", e);
            }
        }
    }

    private void init() {
        try {
            initStorage();
            this.commandRegistry = new CommandRegistry(labWorkService, this);
            requestHandler = new RequestHandler(commandRegistry);

            serverConnectionListener = new ServerConnectionListener(port);
            serverResponseSender = new ServerResponseSender(serverConnectionListener.getSocket());
            handleSaveOnTerminate();
        } catch (Exception e) {
            logger.error("Ошибка, ", e);
            System.exit(1);
        }
    }

    private void handleSaveOnTerminate() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Сервер завершает работу, коллекция сохраняется. Обрабатываем событие Ctrl + C.");
            CommandWithArgument commandWithArgument = new CommandWithArgument(save.name(), save.getBehavior(), null);
            CommandRequest commandRequest = new CommandRequest(commandWithArgument, null, null);
            requestHandler.getCommandController().handle(commandRequest);

            DatagramSocket socket = serverConnectionListener.getSocket();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }));
    }

    private void initStorage() {
        JsonReader jsonReader = new JsonReader();
        Hashtable<String, LabWork> map = new Hashtable<>();
        HashSet<Long> setOfId = new HashSet<>();
        try {
            map = jsonReader.loadFromJson(filename);
            logger.info("Файл успешно загружен: {}", filename);
        } catch (IOException e) {
            logger.error("Ошибка при чтении файла: {}", e.getMessage());
            logger.error("Программа завершается");
            System.exit(1);
        }
        for (LabWork labWork : map.values()) {
            IdCounterService.setId(max(labWork.getId(), IdCounterService.getId()));
            setOfId.add(labWork.getId());
        }
        if (setOfId.size() < map.size()) {
            logger.warn("В файле есть повторяющиеся id — коллекция будет очищена");
            map.clear();
            IdCounterService.setId(0);
        }
        labWorkService = new LabWorkService(map, filename);
    }

    @Override
    public HistoryList getHistoryByClientID(String clientID) {
        return requestHandler.getHistoryByClientID(clientID);
    }
}
