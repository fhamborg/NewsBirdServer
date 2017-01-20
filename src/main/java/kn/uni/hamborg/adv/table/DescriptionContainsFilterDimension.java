/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.table;

import com.google.common.collect.ImmutableList;
import java.util.logging.Logger;

/**
 * Should not be used for instantiation!
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
class DescriptionContainsFilterDimension extends TextContainsFilterDimension {

    private static final Logger LOG = Logger.getLogger(DescriptionContainsFilterDimension.class.getSimpleName());

    public DescriptionContainsFilterDimension(ImmutableList<FilterValue> filterValues) {
        super(filterValues);
    }
}
