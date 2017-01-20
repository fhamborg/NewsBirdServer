/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.table;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A {@link FilterDimension} for recipients.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
class RecipientFilterDimension extends FilterDimension {

    private static final Logger LOG = Logger.getLogger(RecipientFilterDimension.class.getSimpleName());

    public RecipientFilterDimension(ImmutableList<FilterValue> filterValues) {
        super(filterValues);
    }

    public static RecipientFilterDimension createFilterDimension(List<String> recipients) {
        List<FilterValue> filterValues = new ArrayList<>();
        for (int i = 0; i < recipients.size(); i++) {
            String recipient = recipients.get(i);
            filterValues.add(new RecipientFilterValue(i, recipient));
        }
        return new RecipientFilterDimension(ImmutableList.copyOf(filterValues));
    }
}
