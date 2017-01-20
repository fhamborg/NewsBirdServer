/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.io.IOException;
import java.util.logging.Logger;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.lucene.analyzer.AnalyzerFactory;
import kn.uni.hamborg.lucene.analyzer.QueryParserFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.joda.time.DateTime;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class QueryUtils {

    private static final Logger LOG = Logger.getLogger(QueryUtils.class.getSimpleName());

    /**
     * Creates a new query which consists of {@code q} and also (as another
     * mandatory part) a NumericRangeQuery for the specified dates on the given
     * field {@code fieldname}.
     *
     * @param q
     * @param fieldname
     * @param startDate
     * @param endDate
     * @param startInclusive
     * @param endInclusive
     * @return
     */
    public static Query addDateRangeToQuery(Query q, String fieldname,
            DateTime startDate, DateTime endDate,
            boolean startInclusive, boolean endInclusive) {
        if (!fieldname.equals(LightDoc.PUB_DATE)) {
            throw new IllegalArgumentException("not supported yet");
        }

        return addQueryToQuery(q, createNumericRangeQueryForDate(fieldname, startDate, endDate, startInclusive, endInclusive));
    }

    /**
     * Creates a new query which consists of {@code q} and also (as another
     * mandatory part) a NumericRangeQuery for the specified dates on the given
     * field {@code fieldname}. Both dates are inclusive and the used field is
     * {@link LightDoc#PUB_DATE}.
     *
     * @param q
     * @param startDate
     * @param endDate
     * @return
     */
    public static Query addDateRangeToQuery(Query q, DateTime startDate, DateTime endDate) {
        return addDateRangeToQuery(q, LightDoc.PUB_DATE, startDate, endDate, true, true);
    }

    /**
     * Creates a new query which consists of {@code q} and also (as another
     * mandatory part) a TermQuery for the specified country. The used field is
     * {@link LightDoc#PUB_COUNTRY}.
     *
     * @param q
     * @param countryCode
     * @return
     */
    public static Query addPubCountryToQuery(Query q, String countryCode) {
        return addQueryToQuery(q, new TermQuery(new Term(LightDoc.PUB_COUNTRY, countryCode)));
    }

    /**
     * Both queries are combined (both as MUST).
     *
     * @param q
     * @param alsoRequired
     * @return
     */
    public static Query addQueryToQuery(Query q, Query alsoRequired) {
        if (q == null) {
            return alsoRequired;
        }
        if (alsoRequired == null) {
            return q;
        }

        BooleanQuery bq = new BooleanQuery();
        bq.add(q, BooleanClause.Occur.MUST);
        bq.add(alsoRequired, BooleanClause.Occur.MUST);
        return bq;
    }

    /**
     * Creates a numeric range query for a start and an enddate.
     *
     * @param field
     * @param dtStart
     * @param dtEnd
     * @param startInclusive
     * @param endInclusive
     * @return
     */
    public static NumericRangeQuery<Long> createNumericRangeQueryForDate(
            String field, DateTime dtStart, DateTime dtEnd, boolean startInclusive, boolean endInclusive
    ) {
        NumericRangeQuery<Long> numQuery = NumericRangeQuery.newLongRange(
                field,
                dtStart.getMillis(),
                dtEnd.getMillis(),
                startInclusive,
                endInclusive);

        return numQuery;
    }

    public static NumericRangeQuery<Long> createNumericRangeQueryForDate(
            String field, String startDate, String endDate, boolean startInclusive, boolean endInclusive
    ) {
        DateTime start = DateTimeUtils.simpleDateTimeFormatter.parseDateTime(startDate);
        DateTime end = DateTimeUtils.getEndOfDay(DateTimeUtils.simpleDateTimeFormatter.parseDateTime(endDate));
        return createNumericRangeQueryForDate(field, start, end, startInclusive, endInclusive);
    }

    /**
     * Converts scoredocs in their Documents
     *
     * @param scoredocs
     * @param indexReader
     * @return
     */
    public static Document[] scoreDocsToDocuments(ScoreDoc[] scoredocs, IndexReader indexReader) throws IOException {
        Document[] docs = new Document[scoredocs.length];
        for (int i = 0; i < docs.length; i++) {
            docs[i] = indexReader.document(scoredocs[i].doc);
        }
        return docs;
    }

    /**
     * Returns the number of documents matching the query in the Index behind
     * IndexSearcher
     *
     * @param query
     * @param indexSearcher
     * @return
     */
    public static int getCountDocsMatchingQuery(Query query, IndexSearcher indexSearcher) {
        try {
            return indexSearcher.search(query, Integer.MAX_VALUE).scoreDocs.length;
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    public static void main(String[] args) throws Exception {

        Directory d = IndexUtils.openDirectory(LuceneConfig.INDEX_DIR_DEFAULT);
        IndexReader reader = IndexUtils.createIndexReader(d);
        IndexSearcher searcher = IndexUtils.createIndexSearcher(reader);
        Analyzer analyzer = AnalyzerFactory.createCustomAnalyzer();
        QueryParser parser = QueryParserFactory.createQueryParser(analyzer);

        Query q = parser.parse("merkel");
        System.out.println("" + searcher.search(q, 10).scoreDocs.length);

        System.out.println(addPubCountryToQuery(q, "US"));
    }
}
