package ru.lenok.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommandResponse implements Serializable {
    private final String output;
    private final Exception error;

    public CommandResponse(String result) {
        this.output = result;
        this.error = null;
    }

    public CommandResponse(Exception error) {
        this.error = error;
        this.output = null;
    }
}
