/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.table;

import java.util.logging.Logger;
import kn.uni.hamborg.utils.QueryUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
class PublishDateIntervalFilterValue extends FilterValue {

    private static final Logger LOG = Logger.getLogger(PublishDateIntervalFilterValue.class.getSimpleName());

    private final Interval dateInterval;

    /**
     *
     * @param positionInDimension
     * @param filterValue Human readable representation of the time interval,
     * e.g., if it is a whole day, just an easy representation of that day,
     * 20151107
     * @param from, inclusive this
     * @param to, inclusive this
     */
    public PublishDateIntervalFilterValue(int positionInDimension, String filterValue, DateTime from, DateTime to, String fieldname) {
        super(positionInDimension, filterValue);

        if (this.query != null) {
            dateInterval = new Interval(0, Long.MAX_VALUE);
            return;
        }

        this.query = QueryUtils.createNumericRangeQueryForDate(fieldname, from, to, true, true);
        this.dateInterval = new Interval(from, to);
    }

    @Override
    public int compareTo(FilterValue o) {
        if (o instanceof PublishDateIntervalFilterValue) {
            PublishDateIntervalFilterValue other = (PublishDateIntervalFilterValue) o;
            if (dateInterval.isEqual(other.dateInterval)) {
                return 0;
            }
            if (dateInterval.isBefore(other.dateInterval)) {
                return -1;
            }
            if (dateInterval.isAfter(other.dateInterval)) {
                return 1;
            }
        }
        return -1;
    }

}
