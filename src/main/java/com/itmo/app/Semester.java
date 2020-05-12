package com.itmo.app;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;

/**
 * текущий семестр для учебной группы
 */
@Getter
@AllArgsConstructor
public enum Semester implements Serializable {
    THIRD("THIRD"),
    FOURTH("FOURTH"),
    FIFTH("FIFTH"),
    SIXTH("SIXTH"),
    EIGHTH("EIGHTH");

    private String english;

    public static Semester getValueByNumber(String number, String messageIfNull) {
        try {
            int numb = Integer.parseInt(number);
            return Arrays.stream(Semester.values()).filter(f -> f.ordinal() + 1 == numb).findAny().orElse(null);
        } catch (NumberFormatException e) {
            System.out.println(messageIfNull);
            return null;
        }
    }
}
