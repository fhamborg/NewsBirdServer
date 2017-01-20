/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web.cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.topic.TopicScore;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.lucene.search.Query;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class Cell {

    private static final Logger LOG = Logger.getLogger(Cell.class.getSimpleName());

    private final String row;
    private final String column;
    private final Object value;
    private final int countDocs;
    private final int countTopics;
    private final List<String> cellDocumentIds;
    private final List<String> topSummarizationDocumentIds;
    private final List<TopicScore> topics;
    private final List<Integer> topicIds;
    private final int countTotalDocsMatchingCellQuery;
    private final String query;
    private final float sentenceScoreByMatrixLM;
    private final List<StringScore> fullSentences;
    private final Map<String, Set<String>> topSentencesToLightDocIds;

    public Cell(String row, String column, Object value,
            int countTotalDocsMatchingQuery, List<TopicScore> topics,
            List<String> cellDocumentIds,
            List<String> topDocumentIds, Query cellQuery,
            float sentenceScoreByMatrixLM, List<StringScore> fullSentences,
            Map<String, Set<String>> topSentencesToLightDocIds) {
        this.row = row;
        this.column = column;
        this.value = value;
        this.countDocs = cellDocumentIds.size();
        this.query = cellQuery.toString();
        topicIds = new ArrayList<>();

        if (topics == null) {
            this.countTopics = 0;
        } else {
            this.countTopics = topics.size();
            for (TopicScore ts : topics) {
                topicIds.add(ts.getTopic().getId());
            }
        }
        this.topics = topics;
        this.countTotalDocsMatchingCellQuery = countTotalDocsMatchingQuery;
        this.topSummarizationDocumentIds = topDocumentIds;
        this.cellDocumentIds = cellDocumentIds;

        if (Float.isNaN(sentenceScoreByMatrixLM)) {
            this.sentenceScoreByMatrixLM = 1337.1337f;
        } else if (Float.isInfinite(sentenceScoreByMatrixLM)) {
            this.sentenceScoreByMatrixLM = sentenceScoreByMatrixLM > 0 ? Float.MAX_VALUE : Float.MIN_VALUE;
        } else {
            this.sentenceScoreByMatrixLM = sentenceScoreByMatrixLM;
        }

        this.fullSentences = fullSentences;
        this.topSentencesToLightDocIds = topSentencesToLightDocIds;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
