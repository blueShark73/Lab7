package com.itmo.app;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * класс локации, указывает, гле находится админ
 */
@AllArgsConstructor
public class Location implements Serializable {
    private double x;
    private Long y; //Поле не может быть null
    private String name; //Строка не может быть пустой, Поле может быть null

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public Long getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(Long y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Location{" +
                "x=" + x +
                ", y=" + y +
                ", name='" + name + '\'' +
                '}';
    }
}
