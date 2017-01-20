/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.topic;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.table.FilterCell;
import kn.uni.hamborg.adv.table.TableManager;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import kn.uni.hamborg.utils.MapUtils;
import kn.uni.hamborg.utils.QueryUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

/**
 * Provides functions to work with a {@link Topic}.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TopicUtils {

    private static final Logger LOG = Logger.getLogger(TopicUtils.class.getSimpleName());

    /**
     * Returns a Map of all (top) terms with their scores. The score of each
     * term is the sum over all topics containing this term of the product of
     * the topic score and the term score within the topic.
     *
     * @param topics
     * @param onlyTopTerms
     * @return
     */
    public static Map<String, Double> topicScoresToTermProbabilities(List<TopicScore> topics, boolean onlyTopTerms) {
        Map<String, Double> termScores = new HashMap<>();
        if (!onlyTopTerms) {
            throw new IllegalArgumentException("not supported yet");
        }

        for (TopicScore ts : topics) {
            double topicScore = ts.getScore();
            Topic topic = ts.getTopic();

            for (StringScore ss : topic.getTopTermProbabilities()) {
                Double termProb = termScores.get(ss.getValue());
                if (termProb == null) {
                    termProb = 0.0;
                }
                termProb += ss.getScore() * topicScore;
                termScores.put(ss.getValue(), termProb);
            }
        }

        return termScores;
    }

    /**
     * Gets a string that can be used for Lucene querying with terms and weights
     *
     * @param termProbabilities
     * @param allRequired
     * @param allBoostingFactor all terms are boosted by this factor (1.0 means
     * no boosting)
     * @return
     */
    public static String getSortedWeightedTopTermsAsQueryString(List<StringScore> termProbabilities, boolean allRequired, float allBoostingFactor) {
        Map<String, Double> termProbs = new HashMap<>();
        for (StringScore entrySet : termProbabilities) {
            String term = entrySet.getValue();
            Double prob = entrySet.getScore();
            Double tmpProb = termProbs.get(term);
            if (tmpProb == null) {
                tmpProb = 0.0;
            }
            tmpProb = tmpProb + prob;
            termProbs.put(term, tmpProb);
        }

        List<Map.Entry<String, Double>> sortedTotalTermProbs = MapUtils.entriesSortedByValuesAsList(termProbs, true);

        final StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Double> sortedTotalTermProb : sortedTotalTermProbs) {
            String term = sortedTotalTermProb.getKey();
            float prob = (float) (double) sortedTotalTermProb.getValue();

            if (allRequired) {
                sb.append("+");
            }
            sb.append(term);
            sb.append("^");
            sb.append(prob * allBoostingFactor);
            sb.append(" ");
        }

        return sb.toString();
    }

    /**
     * Simple convenience function to retrieve for a given {@link Topic} the top
     * {@code n} documents which satisfy the matrix query (from the
     * tablemanager) and the topic query (from the topic itself).
     *
     * @param topicExtractor
     * @param topic
     * @param tableManager
     * @param n
     * @param indexSearcher
     * @param indexReader
     * @return
     */
    public static Document[] getDocumentsForTopic(TopicExtractor topicExtractor, Topic topic, TableManager tableManager, int n, IndexSearcher indexSearcher, IndexReader indexReader) {
        try {
            // query the index
            final ScoreDoc[] scoreDocs = getScoreDocsForTopic(topicExtractor, topic, tableManager, n, indexSearcher, indexReader);
            final Document[] docs = QueryUtils.scoreDocsToDocuments(scoreDocs, indexReader);
            LOG.log(Level.INFO, "query returned {0} docs for topic id {1}",
                    new Object[]{docs.length, topic.getId()});
            return docs;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ScoreDoc[] getScoreDocsForTopic(TopicExtractor topicExtractor, Topic topic, TableManager tableManager, int n, IndexSearcher indexSearcher, IndexReader indexReader) {
        try {
            final Query matrixQuery = tableManager.getFilterQuery();
            final Query topicQuery = topicExtractor.getQueryForTopic(topic);

            // this ensures that only those documents will be used that actually belong to the cells to which this topic belongs to
            BooleanQuery bq = new BooleanQuery();
            for (FilterCell cell : topic.getCells()) {
                bq.add(cell.getQuery(), BooleanClause.Occur.SHOULD);
            }

            final Query finalQuery = QueryUtils.addQueryToQuery(bq,
                    QueryUtils.addQueryToQuery(matrixQuery, topicQuery));

            // query the index
            final ScoreDoc[] scoreDocs = indexSearcher.search(finalQuery, n).scoreDocs;
            return scoreDocs;
        } catch (ParseException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
