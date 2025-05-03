package ru.lenok.server.connectivity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetAddress;

@AllArgsConstructor
@Data
public class ResponseWithClient {
    private final Object response;
    private final InetAddress clientIp;
    private final int clientPort;
}
