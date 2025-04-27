package ru.lenok.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public final class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private Server() {
        throw new UnsupportedOperationException("This is an utility class and can not be instantiated");
    }

    public static void main(String[] args){
        if (args.length != 1) {
            logger.error("Программа должна запускаться с одним аргументами: файл с конфигурацией");
            System.exit(1);
        }
        try {
            ServerApplication app = new ServerApplication(loadProperties(args[0]));
            app.start();
        } catch (Exception e){
            logger.error("Ошибка: ", e);
            System.exit(1);
        }

    }

    private static Properties loadProperties(String path) throws Exception{
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(path)) {
            properties.load(input);

            String dbHost = properties.getProperty("dbHost");
            String dbPort = properties.getProperty("dbPost");
            String dbUser = properties.getProperty("dbUser");
            String dbPassword = properties.getProperty("dbPassword");
            String listenPort = properties.getProperty("listenPort");
            String initialCollectionPath = properties.getProperty("initialCollectionPath");
        }

        return properties; //TODO валидация
    }
}

