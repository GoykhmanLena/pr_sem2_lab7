package ru.lenok.common.models;

import lombok.Data;

import java.io.Serializable;

@Data
public class Coordinates implements Serializable {
    private double x;
    private Float y; //Поле не может быть null

    public boolean validate() {
        return y != null;
    }
}