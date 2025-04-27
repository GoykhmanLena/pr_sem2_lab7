package ru.lenok.common.models;

import lombok.Data;

import java.io.Serializable;

@Data
public class Discipline implements Serializable {
    private String name; //Поле не может быть null, Строка не может быть пустой
    private long practiceHours;

    public boolean validate() {
        return name != null && !name.equals("");
    }
}