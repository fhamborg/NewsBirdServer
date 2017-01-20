/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv;

import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.scorer.CellNgramScorer;
import kn.uni.hamborg.adv.summary.AdvSummarizer;
import kn.uni.hamborg.adv.table.TableManager;
import kn.uni.hamborg.adv.topic.MalletParallelTopicExtractor;
import kn.uni.hamborg.adv.topic.TopicSummarizer;
import kn.uni.hamborg.adv.topic.TopicTimeOccurrenceLikeliness;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.web.cell.SummaryField;
import kn.uni.hamborg.web.query.QueryCommand;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class AnalysisProcessor {

    private static final Logger LOG = Logger.getLogger(AnalysisProcessor.class.getSimpleName());

    private final AnalysisWorkflow analysisWorkflow;
    private QueryCommand queryCommand;

    public AnalysisProcessor() {
        this(AnalysisWorkflow.createDefaultWorkflow());
    }

    public AnalysisProcessor(AnalysisWorkflow analysisWorkflow) {
        this.analysisWorkflow = analysisWorkflow;
    }

    public AnalysisProcessor withQuery(QueryCommand queryCommand) {
        this.queryCommand = queryCommand;
        AnalysisConfiguration.summarization_OrderSentencesByFirstOccurenceInDoc = queryCommand.isSummarization_OrderSentencesByFirstOccurenceInDoc();
        AnalysisConfiguration.summarization_Lin2002Single_FirstSentencesOnly = queryCommand.isSummarization_Lin2002Single_FirstSentencesOnly();
        AnalysisConfiguration.topicField = queryCommand.getTopicField();

        LOG.log(Level.INFO, "updated {0}", new Object[]{AnalysisConfiguration.class.getSimpleName()});

        return this;
    }

    public AnalysisProcessor buildTable() {
        analysisWorkflow.setTableManager(
                new TableManager(
                        analysisWorkflow.getIndexReader(),
                        analysisWorkflow.getIndexSearcher(),
                        analysisWorkflow.getQueryParser(),
                        queryCommand.getRows(),
                        queryCommand.getColumns(),
                        queryCommand.getFilterQuery()));

        return this;
    }

    /**
     * Topics are onlz calculated on the CONTENT STEMMED field. Check whether
     * that makes actually sense.
     *
     * @return
     */
    public AnalysisProcessor computeTopics() {
        analysisWorkflow.setTopicExtractor(new MalletParallelTopicExtractor(
                analysisWorkflow.getTableManager(),
                AnalysisConfiguration.topicField,
                analysisWorkflow.getQueryParser(),
                queryCommand.getNumberOfTopicsPerCell(),
                queryCommand.getTopicCellDocumentMergeType(),
                analysisWorkflow.getIndexReader(),
                analysisWorkflow.getIndexSearcher(),
                analysisWorkflow.getAnalyzer()
        ));
        /*analysisWorkflow.setTopicExtractor(new IHTMTopicExtractor(
         analysisWorkflow.getTableManager(),
         AnalysisConfiguration.topicField,
         analysisWorkflow.getQueryParser(),
         analysisWorkflow.getIndexReader(),
         analysisWorkflow.getIndexSearcher(),
         analysisWorkflow.getAnalyzer()
         ));*/
        analysisWorkflow.getTopicExtractor().computeTopics();
        LOG.info("finished computation of topics");

        //analysisWorkflow.getTopicExtractor().mergeTopics();
        //LOG.info("finished merging of topics");
        analysisWorkflow.setTopicSummarizer(new TopicSummarizer(
                analysisWorkflow.getTableManager(),
                SummaryField.values(),
                analysisWorkflow.getIndexReader(),
                analysisWorkflow.getIndexSearcher(),
                analysisWorkflow.getAnalyzer(),
                analysisWorkflow.getTopicExtractor()
        ));
        analysisWorkflow.getTopicSummarizer().computeSummaries();
        LOG.info("finished computation of topic summaries");

        TopicTimeOccurrenceLikeliness topicTimeLikeliness = new TopicTimeOccurrenceLikeliness(
                analysisWorkflow.getTopicExtractor(),
                queryCommand,
                analysisWorkflow.getIndexSearcher(),
                analysisWorkflow.getQueryParser()
        );
        topicTimeLikeliness.computeTopicsLikeliness();
        LOG.info("finished computation of topic time occurrence likeliness");

        return this;
    }

    /**
     * For each possible value from {@link SummaryField} we calculate a summary.
     *
     * @return
     */
    public AnalysisProcessor computeSummaries() {
        analysisWorkflow.setSummarizer(new AdvSummarizer(analysisWorkflow.getTableManager(), SummaryField.values(),
                analysisWorkflow.getIndexReader(), analysisWorkflow.getIndexSearcher(),
                analysisWorkflow.getAnalyzer(), analysisWorkflow.getTopicExtractor()));
        analysisWorkflow.getSummarizer().computeSummaries();

        if (AnalysisConfiguration.enabledCellNgramScorer) {
            analysisWorkflow.setCellNgramScorer(
                    new CellNgramScorer(analysisWorkflow.getTableManager(), analysisWorkflow.getIndexSearcher(),
                            LightDoc.TITLE)
            );
            analysisWorkflow.getCellNgramScorer().computeModelOnMatrix();
        }

        return this;
    }

    public AnalysisWorkflow getAnalysisWorkflow() {
        return analysisWorkflow;
    }

}
