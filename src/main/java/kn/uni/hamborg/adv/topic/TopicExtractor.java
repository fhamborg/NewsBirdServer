/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.topic;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.AnalysisConfiguration;
import kn.uni.hamborg.adv.table.FilterCell;
import kn.uni.hamborg.adv.table.TableManager;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import kn.uni.hamborg.utils.LuceneUtils;
import kn.uni.hamborg.utils.MapUtils;
import kn.uni.hamborg.utils.QueryUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * This provides functionality for topic extraction. Currently an easy, simple
 * approach where topics for each cell are computed in an isolated manner from
 * other cells. Future approaches could incorporate other cells, e.g., in same
 * row and column, to enhance distinction.
 *
 * TODO: read the above and do it?
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public abstract class TopicExtractor {

    private static final Logger LOG = Logger.getLogger(TopicExtractor.class.getSimpleName());

    /**
     * The default minimum probability for a topic so that we assign it
     * effectively to a cell.
     */
    protected static final double TOPIC_PROBABILITY_THRESHOLD_ = 0.2;
    /**
     * The minimum probability for a topic to be assigned to a cell. It is
     * suggested to adapt it relating the required number of topics per cell.
     */
    protected double topicProbabilityThreshold = TOPIC_PROBABILITY_THRESHOLD_;

    protected final TableManager tableManager;
    protected final String fieldname;
    protected final QueryParser queryParser;
    protected final IndexReader indexReader;
    protected final IndexSearcher indexSearcher;
    protected final Analyzer analyzer;
    protected ImmutableList<Topic> topics;

    public TopicExtractor(TableManager tableManager, String fieldname, QueryParser queryParser, IndexReader indexReader, IndexSearcher indexSearcher, Analyzer analyzer) {
        this.tableManager = tableManager;
        this.fieldname = fieldname;
        this.queryParser = queryParser;
        this.indexReader = indexReader;
        this.indexSearcher = indexSearcher;
        this.analyzer = analyzer;
    }

    /**
     * Compute topics of the whole table, cell-wise.
     */
    public abstract void computeTopics();

    public static Query createFewRequiredTerms(Analyzer analyzer, String terms, String fieldname, int k) {
        BooleanQuery bq = new BooleanQuery();
        List<String> parsedTerms = LuceneUtils.parseKeywords(analyzer, LightDoc.CONTENT_STEMMED, terms);

        // force first 2 term in any case
        //String tmp = parsedTerms.remove(0);
        //bq.add(new TermQuery(new Term(LightDoc.CONTENT_STEMMED, tmp)), BooleanClause.Occur.MUST);
        //tmp = parsedTerms.remove(0);
        //bq.add(new TermQuery(new Term(topicField, tmp)), BooleanClause.Occur.MUST);
        for (String parsedTerm : parsedTerms) {
            TermQuery tq = new TermQuery(new Term(fieldname, parsedTerm));
            bq.add(tq, BooleanClause.Occur.SHOULD);
        }
        bq.setMinimumNumberShouldMatch(k);

        return bq;
    }

    public static Query buildTopicQuery(String topTerms, String allTerms, Analyzer analyzer,
            String fieldname) {
        Query query = null;
        if (topTerms != null && !topTerms.isEmpty()) {
            query = QueryUtils.addQueryToQuery(query,
                    createFewRequiredTerms(analyzer, topTerms, LightDoc.CONTENT_STEMMED, 3));
        }

        if (allTerms != null && !allTerms.isEmpty()) {
            query = QueryUtils.addQueryToQuery(query, //QUERY_PARSER.parse(userFilterAdditionalTerms));
                    createFewRequiredTerms(analyzer, allTerms, LightDoc.CONTENT_STEMMED, 7)
            );
        }
        return query;
    }

    public static Query getTopicsQueryForCell(FilterCell cell, QueryParser queryParser, Analyzer analyzer) {
        try {
            float maxProb = Float.NEGATIVE_INFINITY;
            Topic maxTopic = null;

            BooleanQuery bq = new BooleanQuery();
            for (TopicScore entrySet : cell.getTopicProbabilities()) {
                Topic topic = entrySet.getTopic();
                float probability = (float) entrySet.getScore();
                if (probability > maxProb) {
                    maxTopic = topic;
                }
                Query topicQuery = buildTopicQuery(StringUtils.join(topic.getTopTerms(), " "),
                        StringUtils.join(topic.getTerms(), " "),
                        analyzer, LightDoc.DESCRIPTION_STEMMED);

//getQueryForTopic(topic, queryParser, AnalysisConfiguration.topicQueryForceAllTopTermsIncluded);
                topicQuery.setBoost(probability);
                bq.add(topicQuery, BooleanClause.Occur.SHOULD);
            }

            if (AnalysisConfiguration.onlyTopTopicUsedForSummarizationQuery) {
                return getQueryForTopic(maxTopic, queryParser, AnalysisConfiguration.topicQueryForceAllTopTermsIncluded);
            }
            //LOG.info("CELLTOPICSQUERY =");
            //LOG.info(bq.toString());
            return bq;
        } catch (ParseException pe) {
            throw new RuntimeException(pe);
        }
    }

    /**
     * Creates a {@link Query} for the given {@link FilterCell} which contains
     * top terms weighed of all top topics which are themselves also weighted.
     *
     * @param cell
     * @return
     */
    public Query getTopicQueryForCell(FilterCell cell) {
        return getTopicsQueryForCell(cell, queryParser, analyzer);
    }

    public static Query getQueryForTopic(Topic topic, QueryParser queryParser, boolean forceTopTermsContained) throws ParseException {
        // new variant: top terms are boosted very much
        BooleanQuery bq = new BooleanQuery();
        if (AnalysisConfiguration.topicQueryHasTopTermsSuperBoosted) {
            Query q = queryParser.parse(TopicUtils.getSortedWeightedTopTermsAsQueryString(topic.getTopTermProbabilities(), forceTopTermsContained, 100.0f));
            bq.add(q, BooleanClause.Occur.MUST);
        }
        String allTermsWithWeights = TopicUtils.getSortedWeightedTopTermsAsQueryString(topic.getTermProbabilities(), false, 1.0f);
        Query allTerms = queryParser.parse(allTermsWithWeights);
        allTerms.setBoost(1);
        bq.add(allTerms, BooleanClause.Occur.SHOULD);
        System.out.println(bq.toString());
        return bq;
    }

    /**
     * Returns a Query for the given topic. This query requires that at least
     * one of the top terms of the topic occur. In addition the terms are
     * weighted by the weights in the topic.
     *
     * @param topic
     * @return
     * @throws ParseException
     */
    protected Query getQueryForTopic(Topic topic) throws ParseException {
        return getQueryForTopic(topic, queryParser, AnalysisConfiguration.topicQueryForceAllTopTermsIncluded);
    }

    /**
     * Returns the topics in a Map with their id as key. Recreation every time
     * this is invoked.
     *
     * @return
     */
    public Map<Integer, Topic> getTopicsAsMap() {
        Map<Integer, Topic> topicsWithId = new TreeMap<>();
        for (Topic t : topics) {
            topicsWithId.put(t.getId(), t);
        }

        return topicsWithId;

    }

    /**
     * Merges topics based on the underlying {@link TopicMerger}.
     */
    public void mergeTopics() {
        /*LOG.info("merging topics...");
         ITopicMerger topicMerger = new TopicMerger(tableManager, indexReader, indexSearcher, this, analyzer);
         topicMerger.mergeTopics();
         */
        //topics = ImmutableList.copyOf(mergedTopics);
    }

    /**
     * Creates mappings between topics and a cell if they are relevant, i.e.,
     * their probability is {@code >= TOPIC_PROBABILITY_THRESHOLD}
     *
     * @param topics
     * @param topicProbabilities
     * @param cell
     * @return Returns those topics that have been used.
     */
    protected Set<Topic> createRelevantTopicCellMappings(
            List<Topic> topics, Map<Topic, Double> topicProbabilities, FilterCell cell) {
        List<TopicScore> topicProbs = new ArrayList<>();
        Set<Topic> usedTopics = new HashSet<>();
        double maxProb = -1;
        Topic maxTopic = null;

        for (Map.Entry<Topic, Double> entrySet : topicProbabilities.entrySet()) {
            Topic topic = entrySet.getKey();
            Double topicProb = entrySet.getValue();
            if (topicProb >= topicProbabilityThreshold) {
                topicProbs.add(new TopicScore(topic, topicProb));
                topic.addCell(cell);
                usedTopics.add(topic);
            }

            // System.out.println(topicProbabilities[i]);
            // we also look at the topic with highest probability, in case no other 
            // topic has a prob >= threshold, we will use that.
            // the topic is then actually added later (line 156)
            if (maxProb < topicProb) {
                maxProb = topicProb;
                maxTopic = topic;
            }
        }
        System.out.println(maxProb);
        if (topicProbs.isEmpty()) {
            LOG.log(Level.WARNING, "no topics in this cell {0}, adding topic with highest probability", cell.getHumanReadableId());
            // if this is the case, we just take any topic with the highest prob, no matter about threshold or anything
            topicProbs.add(new TopicScore(maxTopic, maxProb));
            maxTopic.addCell(cell);
            usedTopics.add(maxTopic);
        }

        MapUtils.sortByTopicScores(topicProbs, true);

        cell.setTopics(topicProbs);

        return usedTopics;
    }

    protected static Topic getTopicById(List<Topic> topics, int id) {
        for (Topic topic : topics) {
            if (topic.getId() == id) {
                return topic;
            }
        }
        return null;
    }
}
