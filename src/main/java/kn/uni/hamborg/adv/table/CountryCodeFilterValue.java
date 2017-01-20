/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.table;

import java.util.logging.Logger;
import kn.uni.hamborg.data.light.LightDoc;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

/**
 * A {@link FilterValue} for one specific country code.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
class CountryCodeFilterValue extends FilterValue {

    private static final Logger LOG = Logger.getLogger(CountryCodeFilterValue.class.getSimpleName());

    private final String fieldname = LightDoc.PUB_COUNTRY;

    protected CountryCodeFilterValue(String countryCode, int positionInDimension) {
        super(positionInDimension, countryCode);

        // check if FilterValue already created a query.
        if (query != null) {
            return;
        }

        this.query = new TermQuery(new Term(fieldname, countryCode));
    }

    @Override
    public int compareTo(FilterValue o) {
        CountryCodeFilterValue other = (CountryCodeFilterValue) o;
        return filterValue.compareTo(other.filterValue);
    }

}
