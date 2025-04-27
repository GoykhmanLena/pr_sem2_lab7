package ru.lenok.client.input;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
@Deprecated
public class MemoryInput extends AbstractInput {
    private final List<String> content;
    private final Iterator<String> iterator;
    public MemoryInput(List<String> content) throws IOException {
        this.content = content;
        this.iterator = content.iterator();
    }

    @Override
    public String readLine(){
        return iterator.hasNext() ? iterator.next(): null;
    }

    @Override
    public void prompt(String message) {
        // No prompt needed for file input
    }

    @Override
    public void close() throws IOException {
    }
}
