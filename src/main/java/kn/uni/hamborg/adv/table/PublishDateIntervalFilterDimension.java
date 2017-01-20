/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.table;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import kn.uni.hamborg.data.light.LightDoc;
import org.apache.lucene.queryparser.classic.QueryParser;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
class PublishDateIntervalFilterDimension extends FilterDimension {

    protected static final String fieldname = LightDoc.PUB_DATE;

    private static final Logger LOG = Logger.getLogger(PublishDateIntervalFilterDimension.class.getSimpleName());

    public PublishDateIntervalFilterDimension(ImmutableList<FilterValue> filterValues) {
        super(filterValues);
    }
}
