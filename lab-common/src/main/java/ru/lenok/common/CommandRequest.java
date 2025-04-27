package ru.lenok.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.lenok.common.models.LabWork;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CommandRequest implements Serializable {
    private CommandWithArgument commandWithArgument;
    private LabWork element;
    private UUID clientID;
}
