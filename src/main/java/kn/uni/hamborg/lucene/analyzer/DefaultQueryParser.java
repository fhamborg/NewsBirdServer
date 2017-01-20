/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.lucene.analyzer;

import kn.uni.hamborg.utils.QueryUtils;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.utils.DateTimeUtils;
import kn.uni.hamborg.knowledge.DBPediaRelatedTermsFinder;
import kn.uni.hamborg.knowledge.IRelatedTermsFinder;
import kn.uni.hamborg.knowledge.WordNetRelatedTermsFinder;
import net.sf.extjwnl.JWNLException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

/**
 * Custom QueryParser that is capable of:
 * <ul>
 * <li>LightDoc.PUB_DATE: process date range queries</li>
 * <li>Any field: If the queryText is not quoted we perform query expansion
 * using {@link DBPediaRelatedTermsFinder} ({@link WordNetRelatedTermsFinder}
 * can also be used)</li>
 * <li>Remove 'bad' characters from String for parse, e.g., /</li>
 * </ul>
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class DefaultQueryParser extends QueryParser {

    private static final Logger LOG = Logger.getLogger(DefaultQueryParser.class.getSimpleName());

    private final boolean isQueryExpansionEnabled = false;
    private final IRelatedTermsFinder termsFinder;

    public DefaultQueryParser(String fieldname, Analyzer analyzer) {
        super(fieldname, analyzer);
        try {
             termsFinder = new WordNetRelatedTermsFinder();
            //termsFinder = new DBPediaRelatedTermsFinder();

        } catch (Exception ex) {
            Logger.getLogger(DefaultQueryParser.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected Query getRangeQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive) throws ParseException {
        if (field.equals(LightDoc.PUB_DATE)) {
            NumericRangeQuery<Long> numQuery = null;
            try {
                numQuery = QueryUtils.createNumericRangeQueryForDate(field,
                        part1, part2,
                        startInclusive, endInclusive);
            } catch (IllegalArgumentException iae) {
                // could be invalid date format since we use our own however the toString of the query above still prints milliseconds
                numQuery = NumericRangeQuery.newLongRange(field, Long.valueOf(part1), Long.valueOf(part2), startInclusive, endInclusive);
            }
            //LOG.info("created NumericRangeQuery " + numQuery + " " + part1 + " " + part2);
            return numQuery;
        } else {
            return super.getRangeQuery(field, part1, part2, startInclusive, endInclusive); //To change body of generated methods, choose Tools | Templates.
        }
    }

    @Override
    protected Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
        // if the queryText is quoted we do not do expansion, as we assume that the user wants to enforce this specific queryText
        if (quoted || !isQueryExpansionEnabled) {
            return super.getFieldQuery(field, queryText, quoted);
        }

        final BooleanQuery bq = new BooleanQuery();
        final String[] relatedTerms = termsFinder.findRelatedQueryTerms(queryText);

        /**
         * Iterate all the expanded terms (including the original term) and add
         * them to a larger BooleanQuery where all the fields should occur. So,
         * in total at least one of them must occur.
         */
        System.out.println("original term ="+queryText);
        for (int i = 0; i < relatedTerms.length; i++) {
            String relatedTerm = relatedTerms[i];
            bq.add(super.getFieldQuery(field, relatedTerm, true), BooleanClause.Occur.SHOULD);
            System.out.println("added related term = " + relatedTerm);
        }

        return bq;
    }

    @Override
    public Query parse(String query) throws ParseException {
        // remove bad chars
        String newQuery = query.replaceAll("/", "");
        if (newQuery.length() != query.length()) {
            LOG.log(Level.WARNING, "removed signs from this query: {0}", query);
        }
        return super.parse(newQuery); //To change body of generated methods, choose Tools | Templates.
    }

    public static void main(String[] args) throws ParseException {
        QueryParser p = QueryParserFactory.createQueryParser();

        // System.out.println(p.parse("contentStemmed:(test  \"hello\")"));
        /**
         * this gets some stuff like "citi ? belfast" which looks strange but is
         * perfectly fine, as the original is city of belfast.
         */
        System.out.println(p.parse("contentStemmed:( Britain)"));

    }

}
