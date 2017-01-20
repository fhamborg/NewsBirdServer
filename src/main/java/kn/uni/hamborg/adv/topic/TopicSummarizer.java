/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.topic;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.summary.Summaries;
import kn.uni.hamborg.adv.summary.Summary;
import kn.uni.hamborg.adv.table.TableManager;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import kn.uni.hamborg.lucene.summarizer.Summarizer;
import kn.uni.hamborg.lucene.summarizer.TopicTtfIdfSummarizer;
import kn.uni.hamborg.utils.LightDocUtils;
import kn.uni.hamborg.web.cell.SummaryField;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

/**
 * Computes human-friendly summarization sentences, that ideally best represent
 * the "content" of a topic. This is not related to a topic scorer.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TopicSummarizer {

    private static final Logger LOG = Logger.getLogger(TopicSummarizer.class.getSimpleName());

    private static final int NUMBER_OF_SUMMARIZATION_SENTENCES_PER_TOPIC = 5;
    public static final int NUMBER_OF_SUMMARIZATION_DOCS = 20;

    private final TableManager tableManager;
    private final SummaryField[] fieldnames;
    private final IndexReader indexReader;
    private final IndexSearcher indexSearcher;
    private final Analyzer analyzer;
    private final TopicExtractor topicExtractor;

    public TopicSummarizer(TableManager tableManager, SummaryField[] fieldnames, IndexReader indexReader,
            IndexSearcher indexSearcher, Analyzer analyzer, TopicExtractor topicExtractor) {
        this.tableManager = tableManager;
        this.fieldnames = new SummaryField[]{SummaryField.CONTENT, SummaryField.TITLE};//fieldnames;
        this.indexReader = indexReader;
        this.indexSearcher = indexSearcher;
        this.analyzer = analyzer;
        this.topicExtractor = topicExtractor;

    }

    /**
     * Computes summarization sentences for each of the topics.
     */
    public void computeSummaries() {
        try {
            //final Query matrixQuery = tableManager.getFilterQuery();

            for (Topic topic : topicExtractor.getTopicsAsMap().values()) {
                final Document[] docs = TopicUtils.getDocumentsForTopic(topicExtractor, topic, tableManager, NUMBER_OF_SUMMARIZATION_DOCS, indexSearcher, indexReader);

                // if there is no sentence for this topic, create an empty summary and continue with next topic
                if (docs.length == 0) {
                    LOG.warning("CREATING EMPTY SUMMARY");
                    topic.addAttribute(Summaries.createEmptySummaries());
                    continue;
                }

                // compute the summarization
                List<TopicScore> tmpTopicScore = new ArrayList<>();
                tmpTopicScore.add(new TopicScore(topic, 1.0));
                final Summarizer topicSummarizer = new TopicTtfIdfSummarizer(docs, analyzer, tmpTopicScore); //new TtfidfSummarizer(docs, analyzer);

                // save it
                Summaries summaries = new Summaries();
                for (Document doc : docs) {
                    summaries.addTopSummarizationDocumentId(LightDocUtils.getId(doc));
                }

                for (SummaryField fieldname : fieldnames) {
                    StringScore[] topSentences = topicSummarizer.getTopKSentences(fieldname.getFieldname(), NUMBER_OF_SUMMARIZATION_SENTENCES_PER_TOPIC);
                    final Summary summary = new Summary(topSentences, null, null);
                    summaries.addSummary(fieldname, summary);
                }
                topic.addAttribute(summaries);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Summary getSummary(Topic topic, String fieldname) {
        return ((Summaries) topic.getAttribute(Summaries.class)).getSummaries().get(SummaryField.fromLightDocField(fieldname));
    }
}
