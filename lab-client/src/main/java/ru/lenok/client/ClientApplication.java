package ru.lenok.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.client.input.AbstractInput;
import ru.lenok.client.input.ConsoleInput;
import ru.lenok.common.commands.CommandBehavior;
import ru.lenok.common.auth.User;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

public class ClientApplication {
    private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class);
    private final InetAddress ip;
    private final int port;
    public static final UUID CLIENT_ID = UUID.randomUUID();
    private Map<String, CommandBehavior> commandDefinitions;
    private final User user;
    private final boolean isRegister;

    public ClientApplication(InetAddress ip, int port, boolean isRegister, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.user = new User(username, password);
        this.isRegister = isRegister;
    }

    public void start() {
        try (AbstractInput input = new ConsoleInput()) {
            ClientConnector clientConnector = new ClientConnector(ip, port);
            commandDefinitions = clientConnector.sendHello(isRegister, user);
            ClientInputProcessor inputProcessor = new ClientInputProcessor(commandDefinitions, clientConnector, user);
            inputProcessor.processInput(input, true);
        } catch (Exception e) {
            logger.error("Ошибка: " + e.getMessage());
        }
    }
}
