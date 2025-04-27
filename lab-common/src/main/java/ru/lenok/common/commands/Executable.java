package ru.lenok.common.commands;

import ru.lenok.common.CommandRequest;
import ru.lenok.common.CommandResponse;

import java.io.IOException;

public interface Executable {
    CommandResponse execute(CommandRequest req)throws IOException;

    String getDescription();
}
