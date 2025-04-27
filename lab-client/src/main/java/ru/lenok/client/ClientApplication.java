package ru.lenok.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.client.input.AbstractInput;
import ru.lenok.client.input.ConsoleInput;
import ru.lenok.common.commands.CommandBehavior;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

public class ClientApplication {
    private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class);
    private final InetAddress ip;
    private final int port;
    public static final UUID CLIENT_ID = UUID.randomUUID();
    private Map<String, CommandBehavior> commandDefinitions;

    public ClientApplication(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void start() {
        try (AbstractInput input = new ConsoleInput()) {
            ClientConnector clientConnector = new ClientConnector(ip, port);
            commandDefinitions = clientConnector.sendHello();
            ClientInputProcessor inputProcessor = new ClientInputProcessor(commandDefinitions, clientConnector);
            inputProcessor.processInput(input, true);
        } catch (Exception e) {
            logger.error("Ошибка: ", e);
        }
    }
}
