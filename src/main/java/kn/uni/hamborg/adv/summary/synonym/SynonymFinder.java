/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.summary.synonym;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.summary.Summaries;
import kn.uni.hamborg.adv.table.FilterCell;
import kn.uni.hamborg.adv.table.FilterValue;
import kn.uni.hamborg.adv.table.TableManager;
import kn.uni.hamborg.adv.topic.TopicExtractor;
import kn.uni.hamborg.web.cell.SummaryField;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class SynonymFinder {

    private static final Logger LOG = Logger.getLogger(SynonymFinder.class.getSimpleName());
    private final TableManager tableManager;
    private final SummaryField[] fieldnames;
    private final IndexReader indexReader;
    private final IndexSearcher indexSearcher;
    private final Analyzer analyzer;
    private final TopicExtractor topicExtractor;

    public SynonymFinder(TableManager tableManager, SummaryField[] fieldnames, IndexReader indexReader,
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

                final Synonyms synonyms = computeSynonyms(cell);
                cell.addAttribute(synonyms);
            }
        }
    }

    /**
     * Simple computation of synonyms.
     *
     * @param cell
     * @return
     */
    public Synonyms computeSynonyms(final FilterCell cell) {
        final List<String> synonymList = new ArrayList<>();
      
      //  cell.get
    
        
        
        return new Synonyms(synonymList.toArray(new String[0]));
    }
}
