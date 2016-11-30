package ru.ekaerovets.pinyindroid.utils;

import java.util.Date;

/**
 * User: dmitry.karyakin
 * Date: 30.11.2016
 */
public class DateUtils {

    public static int getDayIndex() {
        long timeMillis = new Date().getTime();
        return (int) (timeMillis / 1000 / 3600 / 24) - 16200;
    }

}
