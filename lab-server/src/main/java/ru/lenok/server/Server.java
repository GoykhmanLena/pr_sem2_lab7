package ru.lenok.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private Server() {
        throw new UnsupportedOperationException("This is an utility class and can not be instantiated");
    }

    public static void main(String[] args){
        if (args.length != 2) {
            logger.error("Программа должна запускаться с двумя аргументами: файл с коллекцией и серверный порт");
            System.exit(1);
        }
        String strPort = args[1];
        int port = 0;
        try {
            port = Integer.parseInt(strPort);
        } catch (NumberFormatException e) {
            logger.error("Ошибка, не распознан порт: ", e);
            System.exit(1);
        }
        ServerApplication app = new ServerApplication(args[0], port);
        app.start();

    }
}

