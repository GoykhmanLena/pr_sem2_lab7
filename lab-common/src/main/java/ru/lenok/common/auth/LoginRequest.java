package ru.lenok.common.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class LoginRequest implements Serializable {
    private final User user;
    private final boolean isRegister;
}
