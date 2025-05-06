package ru.lenok.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;


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
            String configFile = args[0];
            List<String> configFileContent = Files.readAllLines(Path.of(configFile));
            logger.info("Конфигурационный файл: " + configFileContent);

            Properties properties = loadProperties(configFile);
            ServerApplication app = new ServerApplication(properties);
            app.start();
        } catch (Exception e) {
            logger.error("Ошибка: ", e);
            System.exit(1);
        }
    }

    public static Properties loadProperties(String path) throws Exception {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(path)) {
            properties.load(input);

            String dbHost = properties.getProperty("dbHost");
            String dbPort = properties.getProperty("dbPort");
            String dbUser = properties.getProperty("dbUser");
            String dbSchema = properties.getProperty("dbSchema");
            String dbPassword = properties.getProperty("dbPassword");
            String listenPort = properties.getProperty("listenPort");
            String reinintDB = properties.getProperty("dbReinit");
            String initialCollectionPath = properties.getProperty("initialCollectionPath");

            validateProperties(dbHost, dbPort, dbUser, dbPassword, listenPort, reinintDB, dbSchema);
        } catch (FileNotFoundException e) {
            throw new Exception("Файл конфигурации не найден: " + path, e);
        } catch (IOException e) {
            throw new Exception("Ошибка при чтении файла конфигурации: " + path, e);
        }

        return properties;
    }

    private static void validateProperties(String dbHost, String dbPort, String dbUser, String dbPassword, String listenPort, String reinitDB, String dbSchema) throws Exception {
        if (dbHost == null || dbHost.isEmpty()) {
            throw new Exception("Параметр dbHost обязателен");
        }
        if (!isValidHost(dbHost)) {
            throw new Exception("Параметр dbHost должен быть корректным хостом или IP-адресом");
        }

        if (dbPort == null || dbPort.isEmpty()) {
            throw new Exception("Параметр dbPort обязателен");
        }

        if (dbSchema == null || dbSchema.isEmpty()) {
            throw new Exception("Параметр dbSchema обязателен");
        }

        if (!isValidPort(dbPort)) {
            throw new Exception("Параметр dbPort должен быть числом в диапазоне от 1 до 65535");
        }

        if (dbUser == null || dbUser.isEmpty()) {
            throw new Exception("Параметр dbUser обязателен");
        }

        if (dbPassword == null || dbPassword.isEmpty()) {
            throw new Exception("Параметр dbPassword обязателен");
        }

        if (listenPort == null || listenPort.isEmpty()) {
            throw new Exception("Параметр listenPort обязателен");
        }
        if (!isValidPort(listenPort)) {
            throw new Exception("Параметр listenPort должен быть числом в диапазоне от 1 до 65535");
        }
        if (reinitDB == null || reinitDB.isEmpty()){
            throw new Exception("Параметр reinitDB обязателен");
        }
    }
    private static boolean isValidHost(String host) {
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        String domainPattern = "^[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
        return Pattern.matches(ipPattern, host) || Pattern.matches(domainPattern, host) || "localhost".equals(host);
    }
    private static boolean isValidPort(String port) {
        try {
            int portNumber = Integer.parseInt(port);
            return portNumber > 0 && portNumber <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

