package ru.lenok.server.connectivity;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.common.util.SerializationUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

@Data
public class ServerConnectionListener {
    private static final Logger logger = LoggerFactory.getLogger(ServerConnectionListener.class);
    private final int port;
    private final DatagramSocket socket;

    public ServerConnectionListener(int port) throws Exception {
        this.port = port;
        this.socket = new DatagramSocket(port);
        logger.info("UDP сервер запущен на порту " + port);
    }

    public IncomingMessage listenAndReceiveMessage() throws IOException {
        byte[] buffer = new byte[SerializationUtils.BUFFER_SIZE];
        DatagramPacket packetFromClient = new DatagramPacket(buffer, buffer.length);
        logger.info("Жду сообщения");
        socket.receive(packetFromClient);

        Object dataFromClient = SerializationUtils.INSTANCE.deserialize(packetFromClient.getData());
        logger.info("Получено: " + dataFromClient);

        return new IncomingMessage(dataFromClient, packetFromClient.getAddress(), packetFromClient.getPort());
    }
}
