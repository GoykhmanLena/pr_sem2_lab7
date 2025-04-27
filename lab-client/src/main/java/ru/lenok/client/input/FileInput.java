package ru.lenok.client.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileInput extends AbstractInput {
    private final BufferedReader reader;

    public FileInput(File file) throws IOException {
        this.reader = new BufferedReader(new FileReader(file));
    }

    @Override
    public String readLine() throws IOException {
        return reader.readLine();
    }

    @Override
    public void prompt(String message) {
        // No prompt needed for file input
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
