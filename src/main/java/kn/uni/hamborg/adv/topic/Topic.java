/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.topic;

import com.google.common.base.MoreObjects;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.AttributeHolder;
import kn.uni.hamborg.adv.table.FilterCell;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import kn.uni.hamborg.utils.MapUtils;

/**
 * This represents a topic. A topic consists of a term-probability distribution.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class Topic extends AttributeHolder {

    private static final Logger LOG = Logger.getLogger(Topic.class.getSimpleName());

    /**
     * Maximum number of top terms that best (hopefully) represent a topic.
     */
    public static final int numberTopTerms = 5;
    /**
     * Maximum number of terms that are kept, i.e., that are considered as
     * relevant.
     */
    public static final int numberRelevantTerms = 15;
    /**
     * Minimum probability for a term to be kept in a topic.
     */
    public static final double minimumTermProbability = 0.002; // was 0.001 before

    /**
     * A sorted list of relevant terms and their probabilities.
     */
    private final List<StringScore> termProbabilities;
    /**
     * A sublist of {@code termProbabilities} with top terms only.
     */
    private final List<StringScore> topTermProbabilities;
    /**
     * All relevant terms, sorted by probability
     */
    private final String[] allTerms;
    /**
     * Only most important terms.
     */
    private final String[] topTerms;
    /**
     * The list of cells that this topic belongs to
     */
    private final transient List<FilterCell> cells;
    /**
     * Number of cells this topic belongs to
     */
    private int cellCount = 0;
    private final int id;

    // statistics
    static final boolean TOPIC_TERM_STATS_ENABLED = false;
    static Map<Double, Integer> freqs = new HashMap<>();

    /**
     * Constructs a {@code Topic} and normalizes the weights of the terms to
     * probabilities. Thereby also only relevant terms are kept and they are
     * being sorted by their probability.
     *
     * @param termWeights
     * @param id
     */
    public Topic(List<StringScore> termWeights, int id) {
        this.id = id;

        List<StringScore> tmp = new ArrayList<>();
        double totalWeights = 0.0;
        for (StringScore termWeight : termWeights) {
            totalWeights += termWeight.getScore();
        }

        for (StringScore entrySet : termWeights) {
            String key = entrySet.getValue();
            double value = entrySet.getScore();
            tmp.add(new StringScore(key, value / totalWeights));
        }

        termProbabilities = removeUnprobableTermsAndSort(tmp);

        allTerms = new String[termProbabilities.size()];
        for (int i = 0; i < termProbabilities.size(); i++) {
            allTerms[i] = termProbabilities.get(i).getValue();
        }

        topTerms = new String[Math.min(numberTopTerms, allTerms.length)];
        System.arraycopy(allTerms, 0, topTerms, 0, Math.min(numberTopTerms, allTerms.length));

        topTermProbabilities = termProbabilities.subList(0, Math.min(numberTopTerms, allTerms.length));

        cells = new ArrayList<>();
        
        LOG.info(Arrays.asList(allTerms).toString());
    }

    /**
     * Returns relevant terms that make up this topic
     *
     * @return
     */
    public String[] getTerms() {
        return allTerms;
    }

    public String[] getTopTerms() {
        return topTerms;
    }

    public void addCell(FilterCell cell) {
        cells.add(cell);
        cellCount++;
    }

    public int getCellCount() {
        return cellCount;
    }

    public List<FilterCell> getCells() {
        return cells;
    }

    public List<StringScore> getTermProbabilities() {
        return termProbabilities;
    }

    @Override
    public int hashCode() {
        //return cells.hashCode();
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        //return this.toString().equals(((Topic) obj).toString());
        return this.id == ((Topic) obj).id;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("topTerms", Arrays.asList(topTerms))
                //.add("cells", cells)
                .toString();
    }

    public int getId() {
        return id;
    }

    public List<StringScore> getTopTermProbabilities() {
        return topTermProbabilities;
    }

    private static List<StringScore> removeUnprobableTermsAndSort(List<StringScore> termProbabilities) {
        List<StringScore> filteredMap = new ArrayList<>();
        for (StringScore entrySet : termProbabilities) {
            String key = entrySet.getValue();
            Double value = entrySet.getScore();
            if (value >= Topic.minimumTermProbability) {
                filteredMap.add(entrySet);
            }

            if (TOPIC_TERM_STATS_ENABLED) {
                value = new BigDecimal(String.valueOf(value)).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                Integer c = freqs.getOrDefault(value, 0);
                freqs.put(value, c + 1);
            }
        }

        // now sort
        MapUtils.sortByObjectScores(filteredMap, true);

        // now keep only relevant
        filteredMap = filteredMap.subList(0, Math.min(Topic.numberRelevantTerms, filteredMap.size()));

        LOG.log(Level.INFO, "remaining term count = {0}", filteredMap.size());
        return filteredMap;
    }

    /**
     * Merges the given topics to one new topic. The new topics has the mean
     * term probabilities of all topics and the id of the first topic.
     *
     * @param topics
     * @return
     */
    public static Topic mergeTopics(List<Topic> topics) {
        Map<String, Double> termProbs = new HashMap<>();
        for (Topic topic : topics) {
            for (StringScore ss : topic.getTermProbabilities()) {
                Double prob = termProbs.getOrDefault(ss.getValue(), 0.0);
                prob += ss.getScore();
                termProbs.put(ss.getValue(), prob);
            }
        }
        for (Map.Entry<String, Double> entrySet : termProbs.entrySet()) {
            String key = entrySet.getKey();
            Double value = entrySet.getValue();
            termProbs.put(key, value / topics.size());
        }

        List<StringScore> termProbsSS = MapUtils.mapToStringScoreList(termProbs);

        return new Topic(termProbsSS, topics.get(0).getId());
    }

}
