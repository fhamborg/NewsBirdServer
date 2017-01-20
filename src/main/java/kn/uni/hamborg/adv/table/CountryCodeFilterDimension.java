/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.table;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.lucene.search.Query;

/**
 * A {@link FilterDimension} for a list of country codes.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
class CountryCodeFilterDimension extends FilterDimension {

    private static final Logger LOG = Logger.getLogger(CountryCodeFilterDimension.class.getSimpleName());

    private CountryCodeFilterDimension(ImmutableList<FilterValue> filterValues) {
        super(filterValues);
    }

    public static CountryCodeFilterDimension createFilterDimension(List<String> countryCodes) {
        List<FilterValue> filterValues = new ArrayList<>();
        for (int i = 0; i < countryCodes.size(); i++) {
            String countryCode = countryCodes.get(i);
            filterValues.add(new CountryCodeFilterValue(countryCode, i));
        }
        return new CountryCodeFilterDimension(ImmutableList.copyOf(filterValues));
    }

}
