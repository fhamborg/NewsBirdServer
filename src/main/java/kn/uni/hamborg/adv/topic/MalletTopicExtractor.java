/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.topic;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.InstanceList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import kn.uni.hamborg.adv.table.FilterCell;
import kn.uni.hamborg.adv.table.TableManager;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;

/**
 * A {@link TopicExtractor} based on Mallet. Prepares some Mallet stuff.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public abstract class MalletTopicExtractor extends TopicExtractor {

    /**
     * Determines whether one Mallet instance is made up of one document (and
     * doc topic probabilities are merged later to get the topic probabilities
     * of a cell) or of all cell documents (of that cell).
     */
    public enum CellDocumentMergeType {

        ONE_DOC_ONE_INSTANCE, ALL_DOCS_ONE_INSTANCE
    };

    private static final Logger LOG = Logger.getLogger(MalletTopicExtractor.class.getSimpleName());

    /**
     * Required number of topics per cell. Note that this is only a desired
     * ratio but it is not guaranteed that this is effectively the number of
     * topics per cell. Instead, this is used to calculate the number of total
     * topics.
     */
    protected static final int NUM_TOPICS_PER_CELL_ = 1;

    /**
     * Total number of topics.
     */
    protected final int numTopics;

    /**
     * The instances.
     */
    protected final InstanceList instances;
    /**
     * This is used to store for each cell its instance position (in the list of
     * all instances, i.e., {@code instances}).
     */
    protected final Map<FilterCell, List<Integer>> cellInstancePositions;
    /**
     * How to map between cell documents and mallet instances.
     */
    protected final CellDocumentMergeType cellDocumentMergeType;

    public MalletTopicExtractor(TableManager tableManager, String fieldname, QueryParser queryParser, int numTopicsPerCell, CellDocumentMergeType cellDocumentMergeType, IndexReader indexReader, IndexSearcher indexSearcher,
            Analyzer analyzer) {
        super(tableManager, fieldname, queryParser, indexReader, indexSearcher, analyzer);

        numTopics = numTopicsPerCell * tableManager.getCellCount();
        this.cellDocumentMergeType = cellDocumentMergeType;
        topicProbabilityThreshold = TOPIC_PROBABILITY_THRESHOLD_ / (double) numTopicsPerCell;

        cellInstancePositions = new HashMap<>();

        // Begin by importing documents from text to feature sequences
        List<Pipe> pipeList = new ArrayList<>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        TokenSequenceRemoveStopwords stopwords = new TokenSequenceRemoveStopwords();
        Iterator iter = EnglishAnalyzer.getDefaultStopSet().iterator();
        String[] tmpstopword = new String[EnglishAnalyzer.getDefaultStopSet().size()];
        int i = 0;
        while (iter.hasNext()) {
            char[] word = (char[]) iter.next();
            tmpstopword[i++] = new String(word);
        }
        stopwords.addStopWords(tmpstopword);
        pipeList.add(stopwords);
        pipeList.add(new TokenSequence2FeatureSequence());

        instances = new InstanceList(new SerialPipes(pipeList));
    }

    /**
     * Returns the instance position of the given cell.
     *
     * @param cell
     * @return
     */
    protected List<Integer> getInstancePositionOfCell(FilterCell cell) {
        return cellInstancePositions.get(cell);
    }

}
