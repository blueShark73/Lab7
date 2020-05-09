package com.itmo.app;

import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;

/**
 * форма обучения группы
 */
@AllArgsConstructor
public enum FormOfEducation implements Serializable {
    FULL_TIME_EDUCATION("Очная", "FULL_TIME_EDUCATION"),
    DISTANCE_EDUCATION("Заочная", "DISTANCE_EDUCATION"),
    EVENING_CLASSES("Вечерняя школа", "EVENING_CLASSES");

    private String russian;
    private String english;

    /**
     * аналог valueOf только еще и сообщением об ошибке
     *
     * @param value         - строка, которую ищем
     * @param messageIfNull - сообщение, если не нашли
     */
    public static FormOfEducation getValue(String value, String messageIfNull) {
        try {
            return Arrays.stream(FormOfEducation.values()).filter(f -> f.english.equals(value))
                    .findAny().orElse(null);
        } catch (NumberFormatException ignored){}
        System.out.println(messageIfNull);
        return null;
    }

    public static FormOfEducation getValueByNumber(String number, String messageIfNull) {
        try{
            int numb = Integer.parseInt(number);
            return Arrays.stream(FormOfEducation.values()).filter(f -> f.ordinal()+1==numb).findAny().orElse(null);
        } catch (NumberFormatException e){
            System.out.println(messageIfNull);
            return null;
        }
    }

    public String getEnglish() {
        return english;
    }
}
