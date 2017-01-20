/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv;

import java.io.IOException;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.scorer.CellNgramScorer;
import kn.uni.hamborg.adv.scorer.POSScorer;
import kn.uni.hamborg.adv.summary.AdvSummarizer;
import kn.uni.hamborg.adv.table.TableManager;
import kn.uni.hamborg.adv.topic.TopicExtractor;
import kn.uni.hamborg.adv.topic.TopicSummarizer;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.lucene.analyzer.AnalyzerFactory;
import kn.uni.hamborg.lucene.analyzer.QueryParserFactory;
import kn.uni.hamborg.utils.IndexUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

/**
 * This represents an analysis workflow.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class AnalysisWorkflow {

    private static final Logger LOG = Logger.getLogger(AnalysisWorkflow.class.getSimpleName());

    private final IndexReader indexReader;
    private final IndexSearcher indexSearcher;
    private final QueryParser queryParser;
    private final Analyzer analyzer;
    private TableManager tableManager;
    private TopicExtractor topicExtractor;
    private TopicSummarizer topicSummarizer;
    private AdvSummarizer summarizer;
    private POSScorer parserScorer;
    private CellNgramScorer cellNgramScorer;

    public AnalysisWorkflow(IndexReader indexReader, IndexSearcher indexSearcher, QueryParser queryParser, Analyzer analyzer) {
        this.indexReader = indexReader;
        this.indexSearcher = indexSearcher;
        this.queryParser = queryParser;
        this.analyzer = analyzer;
    }

    public static AnalysisWorkflow createDefaultWorkflow() {
        try {
            Directory dir = IndexUtils.openDirectory(LuceneConfig.INDEX_DIR_DEFAULT);
            IndexReader indexReader = IndexUtils.createIndexReader(dir);
            IndexSearcher indexSearcher = IndexUtils.createIndexSearcher(indexReader);
            Analyzer analyzer = AnalyzerFactory.createCustomAnalyzer();
            QueryParser queryParser = QueryParserFactory.createQueryParser(analyzer);
            return new AnalysisWorkflow(indexReader, indexSearcher, queryParser, analyzer);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the {@link TableManager} instance bound to this
     * {@code AnalysisWorkflow}.
     *
     * @return
     */
    public TableManager getTableManager() {
        return tableManager;
    }

    public void setTableManager(TableManager tableManager) {
        this.tableManager = tableManager;
    }

    public IndexReader getIndexReader() {
        return indexReader;
    }

    public IndexSearcher getIndexSearcher() {
        return indexSearcher;
    }

    public QueryParser getQueryParser() {
        return queryParser;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public TopicExtractor getTopicExtractor() {
        return topicExtractor;
    }

    public TopicSummarizer getTopicSummarizer() {
        return topicSummarizer;
    }

    public void setTopicSummarizer(TopicSummarizer topicSummarizer) {
        this.topicSummarizer = topicSummarizer;
    }

    public void setTopicExtractor(TopicExtractor topicExtractor) {
        this.topicExtractor = topicExtractor;
    }

    public AdvSummarizer getSummarizer() {
        return summarizer;
    }

    public void setSummarizer(AdvSummarizer summarizer) {
        this.summarizer = summarizer;
    }

    public POSScorer getParserScorer() {
        return parserScorer;
    }

    public void setParserScorer(POSScorer parserScorer) {
        this.parserScorer = parserScorer;
    }

    public CellNgramScorer getCellNgramScorer() {
        return cellNgramScorer;
    }

    public void setCellNgramScorer(CellNgramScorer cellNgramScorer) {
        this.cellNgramScorer = cellNgramScorer;
    }

}
