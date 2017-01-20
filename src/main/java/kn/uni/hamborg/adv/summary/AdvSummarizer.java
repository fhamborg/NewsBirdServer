/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.summary;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.table.FilterCell;
import kn.uni.hamborg.adv.table.FilterValue;
import kn.uni.hamborg.adv.table.TableManager;
import kn.uni.hamborg.adv.topic.TopicExtractor;
import kn.uni.hamborg.adv.topic.TopicScore;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import kn.uni.hamborg.lucene.summarizer.Summarizer;
import kn.uni.hamborg.lucene.summarizer.TopicTtfIdfSummarizer;
import kn.uni.hamborg.utils.LightDocUtils;
import kn.uni.hamborg.utils.MapUtils;
import kn.uni.hamborg.utils.QueryUtils;
import kn.uni.hamborg.web.cell.SummaryField;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

/**
 * Provides functionality to create a summary for a {@link FilterCell}.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class AdvSummarizer {

    private static final Logger LOG = Logger.getLogger(AdvSummarizer.class.getSimpleName());

    private static final int NUMBER_OF_SUMMARIZATION_DOCS = 30;
    private static final int NUMBER_OF_SUMMARIZATION_SENTENCES = 30;
    private static final int NUMBER_OF_SUMMARIZATION_TERMS = 40;

    private final TableManager tableManager;
    private final SummaryField[] fieldnames;
    private final IndexReader indexReader;
    private final IndexSearcher indexSearcher;
    private final Analyzer analyzer;
    private final TopicExtractor topicExtractor;

    public AdvSummarizer(TableManager tableManager, SummaryField[] fieldnames, IndexReader indexReader,
            IndexSearcher indexSearcher, Analyzer analyzer, TopicExtractor topicExtractor) {
        this.tableManager = tableManager;
        this.fieldnames = fieldnames;
        this.indexReader = indexReader;
        this.indexSearcher = indexSearcher;
        this.analyzer = analyzer;
        this.topicExtractor = topicExtractor;
    }

    /**
     * Computes summaries for each {@link FilterCell} in the table hold in the
     * {@link TableManager} instance.
     */
    public void computeSummaries() {
        for (FilterValue rowDimension : tableManager.getRowDimension()) {
            for (FilterValue colDimension : tableManager.getColDimension()) {
                final FilterCell cell = tableManager.getCell(rowDimension, colDimension);

                if (cell.getDocuments().size() == 0) {
                    cell.addAttribute(Summaries.createEmptySummaries());
                    continue;
                }

                final Summaries summaries = computeSummary(cell);
                cell.addAttribute(summaries);
            }
        }
    }

    /**
     * Computes a summary for the given {@code cell}.
     *
     * @param cell
     * @return
     */
    protected Summaries computeSummary(FilterCell cell) {
        final Query cellQuery = cell.getQuery();
        final Query cellTopicQuery = topicExtractor.getTopicQueryForCell(cell);

        // TODO: we might need to add additional restrictions here, e.g., date
        final Query finalQuery = QueryUtils.addQueryToQuery(cellQuery, cellTopicQuery);
        LOG.log(Level.INFO, "created summarization query for cell ''{0}'': {1}", new Object[]{cell.getHumanReadableId(), finalQuery.toString()});

        return computeSummariesForCellAndQuery(finalQuery, cell);
    }

    /**
     * Computes summaries for a cell. These are computed on the top
     * NUMBER_OF_SUMMARIZATION_DOCS documents matching the summarization query
     * (consisting of cell query and topic query (of that cell)).
     *
     * @param query
     * @param cell
     * @return
     */
    protected Summaries computeSummariesForCellAndQuery(Query query, FilterCell cell) {
        try {
            final Summaries summaries = new Summaries();

            // query the index
            final ScoreDoc[] scoreDocs = indexSearcher.search(query, NUMBER_OF_SUMMARIZATION_DOCS).scoreDocs;
            final Document[] docs = QueryUtils.scoreDocsToDocuments(scoreDocs, indexReader);
            LOG.log(Level.INFO, "query returned {0} docs for cell {1}", new Object[]{docs.length, cell.getHumanReadableId()});

            // if there are no docs at all, return an empty Summaries instance
            if (docs.length == 0) {
                return Summaries.createEmptySummaries();
            }

            // compute the summarization
            final Summarizer cellSummarizer = new TopicTtfIdfSummarizer(docs, analyzer, cell.getTopicProbabilities());//new TtfidfSummarizer(docs, analyzer);

            for (SummaryField fieldname : fieldnames) {
                final StringScore[] topSentences = cellSummarizer.getTopKSentences(fieldname.getFieldname(), NUMBER_OF_SUMMARIZATION_SENTENCES);
                final StringScore[] topTerms = cellSummarizer.getTopKTokens(fieldname.getFieldname(), NUMBER_OF_SUMMARIZATION_TERMS);
                final StringScore[] topTermsOfTopics = calcTopTermsOfTopics(cell.getTopicProbabilities());
                final Summary summary = new Summary(topSentences, topTerms, topTermsOfTopics, cellSummarizer.getSentenceLightDocIds());
                summaries.addSummary(fieldname, summary);

                /*if (cell.getRowValue().equals("US") && cell.getColumnValue().equals("usa")) {
                 System.out.println("usa");
                 System.out.println(Arrays.asList(topSentences));
                 }*/
            }

            for (Document doc : docs) {
                final String docId = LightDocUtils.getId(doc);
                summaries.addTopSummarizationDocumentId(docId);
            }

            return summaries;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static StringScore[] calcTopTermsOfTopics(ImmutableList<TopicScore> topicProbabilities) {
        Map<String, Double> termScores = new HashMap<>();

        for (TopicScore topicProbability : topicProbabilities) {
            final double topicWeight = topicProbability.getScore();
            for (StringScore stringScore : topicProbability.getTopic().getTopTermProbabilities()) {
                Double score = termScores.get(stringScore.getValue());
                if (score == null) {
                    score = 0.0;
                }
                score += (stringScore.getScore() * topicWeight);
                termScores.put(stringScore.getValue(), score);
            }
        }

        List<StringScore> topTermsTmp = MapUtils.mapToStringScoreList(termScores);
        MapUtils.sortByObjectScores(topTermsTmp, true);

        int min = Math.min(NUMBER_OF_SUMMARIZATION_TERMS, topTermsTmp.size());
        return topTermsTmp.subList(0, min).toArray(new StringScore[0]);
    }
}
