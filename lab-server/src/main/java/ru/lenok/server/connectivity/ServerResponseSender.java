package ru.lenok.server.connectivity;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.lenok.common.util.SerializationUtils;
import ru.lenok.server.request_processing.RequestHandler;

@Data
public class ServerResponseSender implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(ServerResponseSender.class);
    private final SerializationUtils serializer = SerializationUtils.INSTANCE;
    private final DatagramSocket socket;
    private final BlockingQueue<ResponseWithClient> responseQueue;
    private final static int THREAD_COUNT = 5;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);

    public ServerResponseSender(DatagramSocket datagramSocket, BlockingQueue<ResponseWithClient> responseQueue) {
        this.socket = datagramSocket;
        this.responseQueue = responseQueue;
    }

    public void run(){
        logger.info("Запущен ServerResponseSender");
        while(true) {
            try {
                ResponseWithClient response = responseQueue.take();

                threadPool.execute(() -> {
                    try {
                        sendMessageToClient(response.getResponse(), response.getClientIp(), response.getClientPort());
                    } catch (IOException e) {
                        logger.error("Ошибка: " + e);
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        logger.info("Умер ServerResponseSender");
    }
    public void sendMessageToClient(Object response, InetAddress clientIp, int clientPort) throws IOException {
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
            try {
                Thread.sleep(50);
            } catch (InterruptedException e){
                //ignored
            }
            logger.debug("Отправлен чанк " + i + " из " + chunkCount);
        }
        if (chunkCount == 1) {
            logger.debug("Отправлены данные: " + response);
        }
    }
}
