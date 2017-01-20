/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.table;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import kn.uni.hamborg.utils.DateTimeUtils;
import org.joda.time.DateTime;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
class PublishDayFilterDimension extends PublishDateIntervalFilterDimension {

    private static final Logger LOG = Logger.getLogger(PublishDayFilterDimension.class.getSimpleName());

    public PublishDayFilterDimension(ImmutableList<FilterValue> filterValues) {
        super(filterValues);
    }

    public static PublishDayFilterDimension createFilterDimension(List<String> days) {
        List<FilterValue> filterValues = new ArrayList<>();
        for (int i = 0; i < days.size(); i++) {
            String day = days.get(i);
            DateTime tmp = DateTimeUtils.simpleDateTimeFormatter.parseDateTime(day);
            DateTime from = DateTimeUtils.getStartOfDay(tmp);
            DateTime to = DateTimeUtils.getEndOfDay(tmp);
            filterValues.add(new PublishDateIntervalFilterValue(i, day,
                    from, to, fieldname));

        }
        return new PublishDayFilterDimension(ImmutableList.copyOf(filterValues));
    }

}
