package ru.lenok.server.commands;

import ru.lenok.server.utils.HistoryList;

public interface IHistoryProvider {
    HistoryList getHistoryByClientID(Long clientID);
}
