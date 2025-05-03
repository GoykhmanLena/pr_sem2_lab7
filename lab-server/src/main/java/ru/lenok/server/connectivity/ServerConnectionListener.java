package ru.lenok.server.connectivity;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.common.util.SerializationUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

@Data
public class ServerConnectionListener implements Runnable{
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();;
    private static final Logger logger = LoggerFactory.getLogger(ServerConnectionListener.class);
    private final int port;
    private final DatagramSocket socket;
    private final ForkJoinPool pool = new ForkJoinPool();
    private final BlockingQueue<IncomingMessage> messageQueue;
    private ForkJoinPool forkJoinPool;


    public ServerConnectionListener(int port, BlockingQueue<IncomingMessage> messageQueue) throws Exception {
        this.port = port;
        this.socket = new DatagramSocket(port);
        this.messageQueue = messageQueue;
        logger.info("UDP сервер запущен на порту " + port);
    }

    public void run(){
       // logger.info("Запущен ServerConnectionListener");
        forkJoinPool = new ForkJoinPool(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            forkJoinPool.execute(this::listenAndReceiveMessage);
        }
       // logger.info("Умер ServerConnectionListener");
    }

    public void listenAndReceiveMessage(){
        while (true) {
            try {
                byte[] buffer = new byte[SerializationUtils.BUFFER_SIZE];
                DatagramPacket packetFromClient = new DatagramPacket(buffer, buffer.length);

                synchronized (socket) {
                    logger.info("Жду сообщения");
                    socket.receive(packetFromClient);
                }

                Object dataFromClient = SerializationUtils.INSTANCE.deserialize(packetFromClient.getData());
                logger.info("Получено: " + dataFromClient);

                IncomingMessage incomingMessage = new IncomingMessage(dataFromClient, packetFromClient.getAddress(), packetFromClient.getPort());
                messageQueue.add(incomingMessage);
            } catch (IOException e){
                logger.error("Ошибка: " + e);
            }
        }
    }
}
