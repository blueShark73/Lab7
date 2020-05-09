package com.itmo.app;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * класс человека, используется для описания админа группы
 */
@AllArgsConstructor
@NoArgsConstructor
public class Person implements Serializable {
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Long height; //Поле может быть null, Значение поля должно быть больше 0
    private long weight; //Значение поля должно быть больше 0
    private String passportID; //Длина строки не должна быть больше 24, Строка не может быть пустой, Длина строки должна быть не меньше 7, Поле не может быть null
    private Location location; //Поле не может быть null

    public String getName() {
        return name;
    }

    public String getPassportID() {
        return passportID;
    }

    public Location getLocation() {
        return location;
    }

    public void setPassportID(String passportID) {
        this.passportID = passportID;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", passportID='" + passportID + '\'' +
                ", location=" + location.toString() +
                '}';
    }
}
