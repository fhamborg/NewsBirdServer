/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.table;

import java.util.logging.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

/**
 * A {@link FilterValue} for one specific textual filter value in one field.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TextContainsFilterValue extends FilterValue {

    private static final Logger LOG = Logger.getLogger(TextContainsFilterValue.class.getSimpleName());

    private final String fieldname;

    public TextContainsFilterValue(String fieldname, String filterValue, QueryParser queryParser,
            int positionInDimension) {
        super(positionInDimension, filterValue);

        this.fieldname = fieldname;

        // check if FilterValue already created a query.
        if (query != null) {
            return;
        }

        try {
            this.query = queryParser.parse(fieldname + ":" + filterValue);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public int compareTo(FilterValue o) {
        TextContainsFilterValue other = (TextContainsFilterValue) o;
        return filterValue.compareTo(other.filterValue);
    }

}
