package com.itmo.utils;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeAdapter {
    private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public static ZonedDateTime parseToZonedDateTime(java.sql.Date date){
        return date.toLocalDate().atStartOfDay(ZoneId.systemDefault());
    }

    public static String parseToSQLDate(ZonedDateTime zonedDateTime){
        return dateFormat.format(Date.from(zonedDateTime.toInstant()));
    }
}
