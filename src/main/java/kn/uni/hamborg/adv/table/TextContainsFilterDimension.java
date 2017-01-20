/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.table;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import kn.uni.hamborg.lucene.analyzer.QueryParserFactory;
import org.apache.lucene.queryparser.classic.QueryParser;

/**
 * A {@link FilterDimension} for a list of text filter values.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
class TextContainsFilterDimension extends FilterDimension {

    private static final Logger LOG = Logger.getLogger(TextContainsFilterDimension.class.getSimpleName());

    TextContainsFilterDimension(ImmutableList<FilterValue> filterValues) {
        super(filterValues);
    }

    public static FilterDimension createFilterDimension(String fieldname, List<String> textFilterValues) {
        QueryParser qp = QueryParserFactory.createQueryParser(fieldname);
        List<FilterValue> filterValues = new ArrayList<>();
        for (int i = 0; i < textFilterValues.size(); i++) {
            String textValue = textFilterValues.get(i);
            filterValues.add(new TextContainsFilterValue(fieldname, textValue, qp, i));
        }
        return new TextContainsFilterDimension(ImmutableList.copyOf(filterValues));

    }

}
