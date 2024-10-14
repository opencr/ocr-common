package org.ocr.common.utils;

import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class LocaleUtil {
    private LocaleUtil() {
    }

    public static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");

    public static final Charset CHARSET_1252 = Charset.forName("CP1252");

    private static final ThreadLocal<TimeZone> userTimeZone = new ThreadLocal<>();
    private static final ThreadLocal<Locale> userLocale = new ThreadLocal<>();

    public static void setUserTimeZone(TimeZone timezone) {
        userTimeZone.set(timezone);
    }

    public static TimeZone getUserTimeZone() {
        TimeZone timeZone = userTimeZone.get();
        return (timeZone != null) ? timeZone : TimeZone.getDefault();
    }

    public static void resetUserTimeZone() {
        userTimeZone.remove();
    }

    public static void setUserLocale(Locale locale) {
        userLocale.set(locale);
    }

    public static Locale getUserLocale() {
        Locale locale = userLocale.get();
        return (locale != null) ? locale : Locale.getDefault();
    }

    public static void resetUserLocale() {
        userLocale.remove();
    }

    public static Calendar getLocaleCalendar() {
        return getLocaleCalendar(getUserTimeZone());
    }

    public static Calendar getLocaleCalendar(int year, int month, int day) {
        return getLocaleCalendar(year, month, day, 0, 0, 0);
    }

    public static Calendar getLocaleCalendar(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = getLocaleCalendar();
        cal.set(year, month, day, hour, minute, second);
        cal.clear(Calendar.MILLISECOND);
        return cal;
    }

    public static Calendar getLocaleCalendar(TimeZone timeZone) {
        return Calendar.getInstance(timeZone, getUserLocale());
    }
}
