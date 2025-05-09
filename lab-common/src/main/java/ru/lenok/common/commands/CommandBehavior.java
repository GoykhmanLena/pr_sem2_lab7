package ru.lenok.common.commands;


import static ru.lenok.common.commands.ArgType.*;

public enum CommandBehavior {
    STRING_ARG_HAS_ELEM(STRING, null, true, false),
    CLIENT(null, null, false, true),
    SIMPLE(null, null, false, false),
    STRING_ARG_NO_ELEM(STRING, null, false, false),
    LONG_ARG_HAS_ELEM(LONG, null, true, false),
    LONG_ARG_NO_ELEM(LONG, null, false, false),
    LONG_LONG_ARGS_NO_ELEM(LONG, LONG, false, false),
    NO_ARG_HAS_ELEM(null, null, true, false),
    STRING_ARG_NO_ELEM_CLIENT(STRING, null, false, true);
    private final ArgType argType1;
    private final ArgType argType2;
    private final boolean hasElement;
    private final boolean isClient;

    CommandBehavior(ArgType argType1, ArgType argType2, boolean hasElement, boolean isClient) {
        this.argType1 = argType1;
        this.argType2 = argType2;
        this.hasElement = hasElement;
        this.isClient = isClient;
    }

    public boolean hasElement() {
        return hasElement;
    }

    public ArgType getArgType1() {
        return argType1;
    }
    public ArgType getArgType2() {
        return argType2;
    }

    public boolean hasArg1() {
        return this.argType1 != null;
    }
    public boolean has2Args() {
        return argType1 != null && argType2 != null;
    }

    public boolean isClient() {
        return isClient;
    }
}
