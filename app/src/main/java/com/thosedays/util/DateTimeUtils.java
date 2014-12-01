package com.thosedays.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by joey on 14/12/1.
 */
public class DateTimeUtils {
    private static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static SimpleDateFormat sDateFormat = new SimpleDateFormat(DATETIME_FORMAT);
    public static SimpleDateFormat sDateFormatGmt = new SimpleDateFormat(DATETIME_FORMAT);
    static {
        sDateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static int getTimeZoneOffset() {
        return 0;
    }

    public static Date getNow() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    public static Date getDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = new GregorianCalendar(year, month - 1, day, hour, minute, second);
        return calendar.getTime();
    }

    public static TimeZone getTimeZone() {
        Calendar cal = Calendar.getInstance();
        return cal.getTimeZone();
    }

    public static Date toCurrentTimeZone(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date.getTime() + getTimeZone().getRawOffset());
        return calendar.getTime();
    }
}
