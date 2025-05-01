package ru.lenok.common.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class User implements Serializable {
    private final Long id;
    private final String username;
    private final String password;

    public User(String username, String password){
        this.password = password;
        this.username = username;
        id = null;
    }
}
