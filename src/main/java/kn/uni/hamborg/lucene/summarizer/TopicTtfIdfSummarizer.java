/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.lucene.summarizer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.topic.TopicScore;
import kn.uni.hamborg.adv.topic.TopicUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

/**
 * Summarizes text documents based on TTF-IDF score ({@link TtfidfSummarizer})
 * and boosts summarization elements that are / contain top topic terms.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TopicTtfIdfSummarizer extends TtfidfSummarizer {

    private static final Logger LOG = Logger.getLogger(TopicTtfIdfSummarizer.class.getSimpleName());

    private final List<TopicScore> topics;

    public TopicTtfIdfSummarizer(IndexReader indexReader, IndexSearcher indexSearcher, Analyzer analyzer, List<TopicScore> topics) {
        super(indexReader, indexSearcher, analyzer);
        this.topics = topics;
    }

    public TopicTtfIdfSummarizer(Document[] documents, Analyzer analyzer, List<TopicScore> topics) throws IOException {
        super(documents, analyzer);
        this.topics = topics;
    }

    public TopicTtfIdfSummarizer(String[] contents, Analyzer analyzer, List<TopicScore> topics) throws IOException {
        super(contents, analyzer);
        this.topics = topics;
    }

    @Override
    protected Map<String, Double> calculateTermScore(final String field) throws IOException {
        final Map<String, Double> topicTermScores = TopicUtils.topicScoresToTermProbabilities(topics, true);
        final Map<String, Double> ttfIdfTermScores = super.calculateTermScore(field);
               LOG.info(ttfIdfTermScores.toString());

        // now boost the ttfIdfTerms
        for (Map.Entry<String, Double> entrySet : ttfIdfTermScores.entrySet()) {
            String term = entrySet.getKey();
            Double ttfIdfScore = entrySet.getValue();

            Double topicTermScore = topicTermScores.get(term);
            if (topicTermScore == null) {
                topicTermScore = 0.0;
            } else {
                //System.out.println(term + " " + ttfIdfScore + " " + topicTermScore);
            }

            // topic term scores are very small compared to ttfidf score (ttfidf = 5, tts = 0.001) thus multiply with large number
            ttfIdfTermScores.put(term, ttfIdfScore + topicTermScore * 10000);
            // test with topic score only
            //ttfIdfTermScores.put(term, topicTermScore * 10000);
        }

        //System.out.println(ttfIdfTermScores);
        return ttfIdfTermScores;
    }
}
