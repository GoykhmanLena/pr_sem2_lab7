package ru.lenok.client.input;

import java.io.IOException;

public abstract class AbstractInput implements AutoCloseable {
    public abstract String readLine() throws IOException;

    public abstract void prompt(String message);

    @Override
    public abstract void close() throws IOException;
}
