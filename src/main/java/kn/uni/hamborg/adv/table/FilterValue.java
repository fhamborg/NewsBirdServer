/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.table;

import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public abstract class FilterValue implements Comparable<FilterValue> {

    private static final Logger LOG = Logger.getLogger(FilterValue.class
            .getSimpleName());

    /**
     * Indicating that this FilterValue is about all documents.
     */
    public static final String allDocs = "ALL_DOCS";

    /**
     * This needs to be set by each subclass.
     */
    protected transient Query query = null;

    /**
     * The value which we use for filtering.
     */
    protected final String filterValue;

    /**
     * The position within the parent dimension.
     */
    private final int positionInDimension;

    /**
     * The number of documents matching the {@code query} of this.
     */
    private int numberOfDocs;

    /**
     * If filterValue is allDocs we will create an MatchAllDocsQuery.
     *
     * @param positionInDimension
     * @param filterValue
     */
    public FilterValue(int positionInDimension, String filterValue) {
        this.positionInDimension = positionInDimension;
        this.filterValue = filterValue;

        if (filterValue.equals(allDocs)) {
            query = new MatchAllDocsQuery();
        }
    }

    /**
     * Creates and return a query that will yield only {@link Document}s which
     * have this {@code FilterValue}.
     *
     * @return
     */
    public final Query getFilterQuery() {
        return query;
    }

    /**
     * Human readable version of this, also unique
     *
     * @return
     */
    public String getDescriptor() {
        return filterValue;
    }

    public int getNumberOfDocs() {
        return numberOfDocs;
    }

    public void setNumberOfDocs(int numberOfDocs) {
        this.numberOfDocs = numberOfDocs;
    }

    /**
     * Returns the position within the parent {@link FilterDimension}.
     *
     * @return
     */
    public int getPositionInDimension() {
        return positionInDimension;
    }

    @Override
    public String toString() {
        return filterValue;
    }

    @Override
    public int hashCode() {
        return query.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FilterValue) {
            FilterValue other = (FilterValue) obj;
            return query.equals(other.query);
        } else {
            return false;
        }
    }

}
