package ru.lenok.server.utils;

import ru.lenok.server.commands.CommandName;

import java.util.ArrayList;
import java.util.List;


public final class HistoryList {
    private final List<CommandName> historyList = new ArrayList<>();

    public void addCommand(CommandName commandName) {
        historyList.add(commandName);
    }

    public String getLastNCommands(int n) {
        StringBuffer sb = new StringBuffer();
        for (int i = historyList.size() - 1; i >= 0 && i > historyList.size() - n; i--) {
            sb.append(historyList.get(i) + "\n");
        }
        return sb.toString();
    }
}
