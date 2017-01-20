/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.table;

import java.util.logging.Logger;
import kn.uni.hamborg.data.light.LightDoc;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

/**
 * Filter value representing one Recipient.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
class RecipientFilterValue extends FilterValue {

    private static final Logger LOG = Logger.getLogger(RecipientFilterValue.class.getSimpleName());

    public RecipientFilterValue(int positionInDimension, String filterValue) {
        super(positionInDimension, filterValue);

        // check if FilterValue already created a query.
        if (query != null) {
            return;
        }

        this.query = new TermQuery(new Term(LightDoc.RECIPIENTS, filterValue));
    }

    @Override
    public int compareTo(FilterValue o) {
        return this.filterValue.compareTo(o.filterValue);
    }

}
