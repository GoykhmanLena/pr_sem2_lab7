package ru.lenok.common.commands;


import static ru.lenok.common.commands.ArgType.*;

public enum CommandBehavior {
    STRING_ARG_HAS_ELEM(STRING, true, false),
    CLIENT(null, false, true),
    SIMPLE(null, false, false),
    STRING_ARG_NO_ELEM(STRING, false, false),
    LONG_ARG_HAS_ELEM(LONG, true, false),
    NO_ARG_HAS_ELEM(null, true, false),
    STRING_ARG_NO_ELEM_CLIENT(STRING, false, true);
    private final ArgType argType;
    private final boolean hasElement;
    private final boolean isClient;

    CommandBehavior(ArgType argType, boolean hasElement, boolean isClient) {
        this.argType = argType;
        this.hasElement = hasElement;
        this.isClient = isClient;
    }

    public boolean hasElement() {
        return this.hasElement;
    }

    public ArgType getArgType() {
        return this.argType;
    }

    public boolean hasArg() {
        return this.argType != null;
    }

    public boolean isClient() {
        return this.isClient;
    }
}
