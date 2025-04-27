package ru.lenok.server.connectivity;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import ru.lenok.common.util.SerializationUtils;

@Data
public class ServerResponseSender {
    private static final Logger logger = LoggerFactory.getLogger(ServerResponseSender.class);
    private final SerializationUtils serializer = SerializationUtils.INSTANCE;
    private final DatagramSocket socket;

    public ServerResponseSender(DatagramSocket datagramSocket) {
        this.socket = datagramSocket;
    }

    public void sendMessageToClient(Object response, InetAddress clientIp, int clientPort) throws IOException, InterruptedException {
        SerializationUtils.ChunksWithCRC chunksWithCRC = serializer.serializeAndSplitToChunks(response);
        List<byte[]> chunks = chunksWithCRC.getChunks();
        int chunkCount = chunks.size();
        SerializationUtils.ChunksCountWithCRC chunksCountWithCRC = new SerializationUtils.ChunksCountWithCRC(chunkCount, chunksWithCRC.getCrc());
        byte[] chunksCountWithCRCSerialized = serializer.serialize(chunksCountWithCRC);

        DatagramPacket responsePacket = new DatagramPacket(chunksCountWithCRCSerialized, chunksCountWithCRCSerialized.length, clientIp, clientPort);
        socket.send(responsePacket);
        logger.debug("Отправлено количество чанков и чексумма: " + chunksCountWithCRC);
        int i = 0;
        for (byte[] responseDataChunk : chunks) {
            responsePacket = new DatagramPacket(responseDataChunk, responseDataChunk.length, clientIp, clientPort);
            socket.send(responsePacket);
            i++;
            Thread.sleep(50);
            logger.debug("Отправлен чанк " + i + " из " + chunkCount);
        }
        if (chunkCount == 1) {
            logger.debug("Отправлены данные: " + response);
        }
    }
}
