package ru.lenok.client;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;
import ru.lenok.common.commands.CommandBehavior;
import ru.lenok.common.util.SerializationUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static ru.lenok.client.ClientApplication.CLIENT_ID;
import static ru.lenok.common.util.SerializationUtils.BUFFER_SIZE;
import static ru.lenok.common.util.SerializationUtils.INSTANCE;

@AllArgsConstructor
public class ClientConnector {
    private static final Logger logger = LoggerFactory.getLogger(ClientConnector.class);
    public static final int INTER_WAIT_SLEEP_TIMEOUT = 50;
    private final InetAddress ip;
    private final int port;
    private static final int RETRY_COUNT = 10;
    private static final int WAIT_TIMEOUT = 1000 * 10;
    InetSocketAddress serverAddress;

    public ClientConnector(InetAddress ip, int port) {
        serverAddress = new InetSocketAddress(ip, port);
        this.ip = ip;
        this.port = port;
    }

    /*  private Object sendDataOld(Object obj) throws IOException {
        int retryCount = RETRY_COUNT;
        while (retryCount > 0) {
            try (DatagramSocket socket = new DatagramSocket();) {
                byte[] data = serialize(obj);
                DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
                logger.debug("передаю данные: " + obj);
                socket.send(packet);

                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                socket.setSoTimeout(WAIT_TIMEOUT);
                socket.receive(responsePacket);
                Object response = deserialize(responsePacket.getData());
                logger.debug("Ответ от сервера: " + response);
                return response;
            } catch (SocketTimeoutException e) {
                retryCount--;
                e.printStackTrace();
                if (retryCount == 0) {
                    throw e;
                }
                logger.error("Ошибка, повторяю попытку отправить, попытка: " + (RETRY_COUNT - retryCount + 1));
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        return null;
    }
*/
    private Object sendData(Object obj) {
        int retryCount = RETRY_COUNT;
        while (retryCount > 0) {
            retryCount--;
            try (DatagramChannel clientChannel = DatagramChannel.open()) {
                clientChannel.configureBlocking(false);

                byte[] data = INSTANCE.serialize(obj);
                ByteBuffer buffer = ByteBuffer.wrap(data);

                clientChannel.send(buffer, serverAddress);
                logger.debug("Данные отправлены серверу: " + obj);

                ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                InetSocketAddress sourceAddress = waitForResponse(clientChannel, receiveBuffer);
                if (sourceAddress != null) {
                    //receiveBuffer.flip();
                    Object response = INSTANCE.deserialize(receiveBuffer.array());
                    long expectedChunksSize;
                    List<byte[]> chunks = new ArrayList<>();
                    if (response instanceof SerializationUtils.ChunksCountWithCRC) {
                        SerializationUtils.ChunksCountWithCRC chunksCountWithCRC = (SerializationUtils.ChunksCountWithCRC) response;
                        expectedChunksSize = chunksCountWithCRC.getChunksCount();
                        logger.debug("Ответ от сервера, ожидается количество чанков: " + expectedChunksSize);
                        for (int i = 1; i <= expectedChunksSize; i++) {
                            receiveBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                            receiveBuffer.clear();
                            sourceAddress = waitForResponse(clientChannel, receiveBuffer);
                            if (sourceAddress != null) {
                                chunks.add(INSTANCE.copy(receiveBuffer.array(), receiveBuffer.position()));
                                logger.debug("Ответ от сервера, получен чанк: " + i);
                            }
                        }
                        if (expectedChunksSize == chunks.size()) {
                            response = INSTANCE.deserializeFromChunks(chunks, chunksCountWithCRC.getCrc());
                            if (expectedChunksSize == 1) {
                                logger.debug("Ответ от сервера: " + response);
                            }
                            else {
                                logger.debug("Ответ от сервера: итого получено чанков " + expectedChunksSize);
                            }
                            return response;
                        }
                        else {
                            logger.error("Ожидалось " + expectedChunksSize +  " чанков, а пришло: " + chunks.size());
                            continue;
                        }
                    }
                    else {
                        logger.error("Ожидалось количество чанков, а пришло другое: " + response);
                    }
                    return response;
                } else {
                    if (retryCount > 0) {
                        logger.warn("Сервер не ответил, повторяю попытку отправить, попытка: " + (RETRY_COUNT - retryCount + 1) + " из " + RETRY_COUNT);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalArgumentException("Сервер недоступен");
    }

    private static InetSocketAddress waitForResponse(DatagramChannel clientChannel, ByteBuffer receiveBuffer) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        InetSocketAddress sourceAddress = null;

        while ((System.currentTimeMillis() - startTime) < WAIT_TIMEOUT) {
            receiveBuffer.clear();
            sourceAddress = (InetSocketAddress) clientChannel.receive(receiveBuffer);

            if (sourceAddress != null) {
                break;
            }

            Thread.sleep(INTER_WAIT_SLEEP_TIMEOUT);
        }
        return sourceAddress;
    }

    public Map<String, CommandBehavior> sendHello() {
        Object commandDefinitions = sendData(CLIENT_ID);
        if (commandDefinitions instanceof Map) {
            return (Map<String, CommandBehavior>) commandDefinitions;
        }
        throw new IllegalArgumentException("Неверный ответ от сервера на команду приветствия: " + commandDefinitions);
    }

    public CommandResponse sendCommand(CommandRequest commandRequest) {
        Object commandResponse = sendData(commandRequest);
        if (commandResponse instanceof CommandResponse) {
            return (CommandResponse) commandResponse;
        }
        throw new IllegalArgumentException("Неверный ответ от сервера на команду: " + commandResponse);
    }


}
