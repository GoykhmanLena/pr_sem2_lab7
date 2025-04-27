package ru.lenok.server.connectivity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetAddress;
@AllArgsConstructor
@Data
public class IncomingMessage {
    private final Object message;
    private final InetAddress clientIp;
    private final int clientPort;
}
