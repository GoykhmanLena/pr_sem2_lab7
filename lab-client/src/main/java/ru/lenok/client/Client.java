package ru.lenok.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public final class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);


    private Client() {
        throw new UnsupportedOperationException("This is an utility class and can not be instantiated");
    }


    public static void main(String[] args) {
        if (!(args.length == 5 || args.length == 6)) {
            printUsageAndExit();
        }

        String host = null;
        int port = -1;
        String username = null;
        String password = null;
        boolean isRegistration = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-u":
                    if (++i < args.length) {
                        username = args[i];
                    } else {
                        printUsageAndExit();
                    }
                    break;
                case "-p":
                    if (++i < args.length) {
                        password = args[i];
                    } else {
                        printUsageAndExit();
                    }
                    break;
                case "-r":
                    isRegistration = true;
                    break;
                default:
                    String[] hostPort = args[i].split(":");
                    if (hostPort.length != 2) {
                        printUsageAndExit();
                    }
                    host = hostPort[0];
                    try {
                        port = Integer.parseInt(hostPort[1]);
                    } catch (NumberFormatException e) {
                        printUsageAndExit();
                    }
                    break;
            }
        }

        if (host == null || username == null || password == null || port == -1) {
            printUsageAndExit();
        }

        if (!isValidHost(host)) {
            logger.info("Ошибка: Неверный формат хоста: " + host);
            System.exit(1);
        }

        if (!isValidPort(port)) {
            logger.info("Ошибка: Неверный формат порта: " + port);
            System.exit(1);
        }

        logger.info("Хост: " + host);
        logger.info("Порт: " + port);
        logger.info("Имя пользователя: " + username);
        logger.info("Пароль: " + password);
        logger.info("Режим регистрации: " + (isRegistration ? "Да" : "Нет"));
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            logger.error("Ошибка: ", e);
            System.exit(-1);
        }

        ClientApplication app = new ClientApplication(ip, port, isRegistration, username, password);
        app.start();
    }

    private static boolean isValidHost(String host) {
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        String domainPattern = "^[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
        return Pattern.matches(ipPattern, host) || Pattern.matches(domainPattern, host) || "localhost".equals(host);
    }

    private static boolean isValidPort(int port) {
        return port >= 1 && port <= 65535;
    }

    private static void printUsageAndExit() {
        logger.info("Использование: <host>:<port> -u <username> -p <password> [-r]");
        System.exit(1);
    }
}
