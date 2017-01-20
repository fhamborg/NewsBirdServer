/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.utils.LightDocUtils;
import kn.uni.hamborg.lucene.analyzer.AnalyzerFactory;
import kn.uni.hamborg.lucene.analyzer.QueryParserFactory;
import kn.uni.hamborg.utils.IndexUtils;
import kn.uni.hamborg.lucene.index.ScoreDocToDocConverter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.joda.time.DateTime;

/**
 * This class provides functionality to retrieve EAs for a given event query.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EventArticleExtractor {

    private static final Logger LOG = Logger.getLogger(EventArticleExtractor.class.getSimpleName());
    /**
     * The maximum number of results that are returned by query. First
     * experiments have showed that usually not more than 400 results are
     * actually relevant, so 1000 should be large enough to not miss anything.
     */
    private static final int numberSearchResults = 1000;

    public static void main(String[] args) throws Exception {
        new EventArticleExtractor().test();
    }

    private void test() {
        try {
            EventArticleExtractor eaExtractor = new EventArticleExtractor();
            eaExtractor.getEventArticles("+pubDate:[20141103 TO 20141111] "
                    + "+contentStemmed:russia* +contentStemmed:ukrain* "
                    + "+(titleStemmed:troops titleStemmed:cross titleStemmed:tanks)");
        } catch (IOException ex) {
            Logger.getLogger(EventArticleExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private final Directory directory;
    private final IndexReader indexReader;
    private final IndexSearcher indexSearcher;
    private final Analyzer analyzer;
    private final QueryParser queryParser;

    public EventArticleExtractor() throws IOException {
        this(LuceneConfig.INDEX_DIR_DEFAULT);
    }

    public EventArticleExtractor(final File indexDir) throws IOException {
        this(IndexUtils.openDirectory(indexDir));
    }

    public EventArticleExtractor(Directory indexDir, IndexReader indexReader,
            IndexSearcher indexSearcher, Analyzer analyzer, QueryParser queryParser) {
        this.directory = indexDir;
        this.indexReader = indexReader;
        this.indexSearcher = indexSearcher;
        this.analyzer = analyzer;
        this.queryParser = queryParser;
    }

    public EventArticleExtractor(final Directory indexDir) throws IOException {
        directory = indexDir;
        indexReader = IndexUtils.createIndexReader(indexDir);
        indexSearcher = IndexUtils.createIndexSearcher(indexReader);
        analyzer = AnalyzerFactory.createCustomAnalyzer();
        queryParser = QueryParserFactory.createQueryParser(analyzer);
    }

    /**
     * Returns {@link Document}s representing the queried event.
     *
     * @param query
     * @return
     */
    public EventArticleResult getEventArticles(final String query) {
        try {
            final Query luceneQuery = queryParser.parse(query);
            LOG.info("parsed Lucene query: " + luceneQuery.toString());

            final TopDocs topDocs = indexSearcher.search(luceneQuery, numberSearchResults);
            final ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            LOG.info("hits: recieved = " + topDocs.totalHits + ", requested maximum = " + numberSearchResults);

            final Document[] documents = ScoreDocToDocConverter.toDocuments(indexReader, scoreDocs);
            LOG.info("converted to " + documents.length + " Lucene documents");

            EventArticleResult result = selectEAByPeakDateAndNextDays(documents, 1);
            result.setQuery(query);

            //   Summarizer summary = new TtfidfSummarizer(selectedEADocuments, analyzer);
            //   for (String s : summary.getTopKSentences(LightDoc.CONTENT, 10)) {
            //     System.out.println(s + "\n");
            //  }
            return result;
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        } catch (IOException ioex) {
            LOG.log(Level.SEVERE, ioex.getMessage(), ioex);
            return null;
        }
    }

    /**
     * Selects EA articles by finding the peak date. The articles of this peak
     * date and following {@code nextDays} days afterwards are then returned.
     *
     * @param docs
     * @param nextDays Number of days which are additionally being returned.
     * @return
     */
    private EventArticleResult selectEAByPeakDateAndNextDays(final Document[] docs, int nextDays) {
        Map<DateTime, ArrayList<Document>> docsByDate = new HashMap<>();

        for (Document doc : docs) {
            // round down the date to the beginning of the day so that we can easily find the date peak
            DateTime roundedDownDate = LightDocUtils.getPubDate(doc).withTimeAtStartOfDay();
            ArrayList<Document> tmpDateDocs = docsByDate.get(roundedDownDate);
            if (tmpDateDocs == null) {
                tmpDateDocs = new ArrayList<>();
                docsByDate.put(roundedDownDate, tmpDateDocs);
            }
            tmpDateDocs.add(doc);
        }

        // now we have all the documents per each day -> get the peak date!
        DateTime peakDate = null;
        int peakArticleCount = 0;
        for (Map.Entry<DateTime, ArrayList<Document>> entrySet : docsByDate.entrySet()) {
            DateTime key = entrySet.getKey();
            ArrayList<Document> value = entrySet.getValue();

            if (value.size() > peakArticleCount) {
                peakArticleCount = value.size();
                peakDate = key;
            }
        }

        if (peakDate == null) {
            throw new RuntimeException("A peak date was not found, probably no article was given");
        }

        LOG.info("peak date = " + peakDate + ", peak date article count = " + peakArticleCount + "\n"
                + "additionally extracting articles of the next " + nextDays + " days");

        List<Document> selectedDocs = new ArrayList<>();
        for (int i = 0; i <= nextDays; i++) {
            DateTime curDate = peakDate.plusDays(i);
            // System.out.println("" + curDate);
            List<Document> docsOnDate = docsByDate.get(curDate);
            // System.out.println("docs on date = " + docsOnDate.size());
            selectedDocs.addAll(docsOnDate);
        }

        LOG.info("selected " + selectedDocs.size() + " documents");

        return new EventArticleResult(
                selectedDocs.toArray(new Document[0]),
                peakDate, null);
    }

    private void selectEAByDifferentCountries() {
        // TODO
    }
}
