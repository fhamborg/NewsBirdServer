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
class TitleContainsFilterDimension extends TextContainsFilterDimension {

    private static final Logger LOG = Logger.getLogger(TitleContainsFilterDimension.class.getSimpleName());

    public TitleContainsFilterDimension(ImmutableList<FilterValue> filterValues) {
        super(filterValues);
    }
}
