package ru.lenok.client.input;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleInput extends AbstractInput {
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public String readLine() throws IOException {
        return reader.readLine();
    }

    @Override
    public void prompt(String message) {
        System.out.print(message);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}

