package ru.lenok.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.collection.LabWorkService;
import ru.lenok.server.commands.CommandRegistry;
import ru.lenok.server.commands.IHistoryProvider;
import ru.lenok.server.connectivity.IncomingMessage;
import ru.lenok.server.connectivity.ResponseWithClient;
import ru.lenok.server.connectivity.ServerConnectionListener;
import ru.lenok.server.connectivity.ServerResponseSender;
import ru.lenok.server.daos.DBConnector;
import ru.lenok.server.daos.LabWorkDAO;
import ru.lenok.server.daos.UserDAO;
import ru.lenok.server.request_processing.RequestHandler;
import ru.lenok.server.services.UserService;
import ru.lenok.server.utils.HistoryList;
import ru.lenok.server.utils.JsonReader;

import java.io.IOException;
import java.net.DatagramSocket;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerApplication implements IHistoryProvider {

    private static final Logger logger = LoggerFactory.getLogger(ServerApplication.class);
    private LabWorkService labWorkService;
    private CommandRegistry commandRegistry;
    private Thread requestHandlerThread;
    private RequestHandler reqHandler;
    private int port;
    private final Properties properties;
    private Thread serverConnectionListenerThread;
    private ServerConnectionListener serverConListener;
    private Thread serverResponseSenderThread;
    private ServerResponseSender serverRespSender;
    private UserService userService;
    private final BlockingQueue<IncomingMessage> incomingMessageQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ResponseWithClient> responseQueue = new LinkedBlockingQueue<>();

    public ServerApplication(Properties properties) {
        this.properties = properties;
        init();
    }

    public void start() {
        logger.info("Сервер работает");
            try {
                serverConListener.run();
                requestHandlerThread.start();
                serverResponseSenderThread.start();
            } catch (Exception e) {
                logger.error("Ошибка, ", e);
            }
    }

    private void init() {
        try {
            port = Integer.parseInt(properties.getProperty("listenPort"));
        } catch (NumberFormatException e) {
            logger.error("Ошибка, не распознан порт: ", e);
            System.exit(1);
        }

        try {
            initServices();
            this.commandRegistry = new CommandRegistry(labWorkService, this);

            reqHandler =  new RequestHandler(commandRegistry, userService, responseQueue, incomingMessageQueue);
            requestHandlerThread = new Thread(reqHandler);

            serverConListener = new ServerConnectionListener(port, incomingMessageQueue);
           // serverConnectionListenerThread = new Thread(serverConListener);

            serverRespSender = new ServerResponseSender(serverConListener.getSocket(), responseQueue);
            serverResponseSenderThread = new Thread(serverRespSender);
            handleSaveOnTerminate();
        } catch (Exception e) {
            logger.error("Ошибка, ", e);
            System.exit(1);
        }
    }

    private void handleSaveOnTerminate() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Сервер завершает работу. Обрабатываем событие Ctrl + C.");

            DatagramSocket socket = serverConListener.getSocket();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }));
    }

    private void initServices() {
        String dbPort = properties.getProperty("dbPort");
        String dbUser = properties.getProperty("dbUser");
        String dbPassword = properties.getProperty("dbPassword");
        String dbHost = properties.getProperty("dbHost");
        String dbSchema = properties.getProperty("dbSchema");
        String dbReinit = properties.getProperty("dbReinit");
        String filename = properties.getProperty("initialCollectionPath");

        boolean reinitDB = Boolean.parseBoolean(dbReinit);

        Hashtable<String, LabWork> initialState = new Hashtable<>();
        if (reinitDB && filename != null && !filename.isEmpty()) {

            JsonReader jsonReader = new JsonReader();
            HashSet<Long> setOfId = new HashSet<>();
            try {
                initialState = jsonReader.loadFromJson(filename);
                logger.info("Файл успешно загружен: {}", filename);
            } catch (IOException e) {
                logger.error("Ошибка при чтении файла: {}", e.getMessage());
                logger.error("Программа завершается");
                System.exit(1);
            }
            for (LabWork labWork : initialState.values()) {
                setOfId.add(labWork.getId());
            }
            if (setOfId.size() < initialState.size()) {
                logger.warn("В файле есть повторяющиеся id — коллекция будет очищена");
                initialState.clear();
            }
        }


        try {
            DBConnector dbConnector = new DBConnector(dbHost, dbPort, dbUser, dbPassword, dbSchema);
            UserDAO userDAO = new UserDAO(getUserIdsFromLabWorks(initialState), dbConnector, reinitDB);
            LabWorkDAO labWorkDAO = new LabWorkDAO(initialState, dbConnector, reinitDB, dbSchema);
            userService = new UserService(userDAO);
            labWorkService = new LabWorkService(labWorkDAO);
        } catch (SQLException | NoSuchAlgorithmException e) {
            logger.error("Ошибка при инициализации сервисов: {} {}", e.getMessage());
            e.printStackTrace();
            logger.error("Программа завершается");
            System.exit(1);
        }
    }

    private Set<Long> getUserIdsFromLabWorks(Map<String, LabWork> initialState) {
        Set<Long> result = new HashSet<>();
        for (LabWork labWork : initialState.values()) {
            result.add(labWork.getOwnerId());
        }
        return result;
    }

    @Override
    public HistoryList getHistoryByClientID(Long clientID) {
        return reqHandler.getHistoryByClientID(clientID);
    }
}
