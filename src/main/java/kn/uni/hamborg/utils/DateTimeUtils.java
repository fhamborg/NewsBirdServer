/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class DateTimeUtils {

    private static final Logger LOG = Logger.getLogger(DateTimeUtils.class.getSimpleName());

    /**
     * In format YYYYMMdd
     */
    public static final DateTimeFormatter simpleDateTimeFormatter = DateTimeFormat.forPattern("YYYYMMdd");

    public static String toYMDString(DateTime d) {
        return simpleDateTimeFormatter.print(d);
    }

    public static DateTime getEndOfDay(DateTime d) {
        return d.plusDays(1).withTimeAtStartOfDay().minusMillis(1);
    }

    public static DateTime getStartOfDay(DateTime d) {
        return d.withTimeAtStartOfDay();
    }

}
