/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.knowledge;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.FileWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.lucene.analyzer.QueryParserFactory;
import kn.uni.hamborg.utils.DateTimeUtils;
import kn.uni.hamborg.utils.IndexUtils;
import kn.uni.hamborg.utils.QueryUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.joda.time.DateTime;

/**
 * This measures the frequency of articles for each country over the complete
 * dataset.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TopicFrequencyStats {
    
    private static final Logger LOG = Logger.getLogger(TopicFrequencyStats.class.getSimpleName());
    
    private void run() throws Exception {
        IndexReader ir = IndexUtils.createIndexReader(IndexUtils.openDirectory(LuceneConfig.INDEX_DIR_2014_ONLY_EN));
        IndexSearcher is = IndexUtils.createIndexSearcher(ir);
        CSVWriter csv = new CSVWriter(new FileWriter("topicfreqs.csv"));
        final QueryParser qp = QueryParserFactory.createQueryParser(LightDoc.DESCRIPTION_STEMMED);
        final Query topicQuery = qp.parse("people time 2015 world year");//qp.parse("iphone apple retina ios");

        DateTime start = new DateTime(2014, 1, 1, 0, 0);
        
        for (int i = 0; i < 365; i++) {
            NumericRangeQuery<Long> numQuery = QueryUtils.createNumericRangeQueryForDate(LightDoc.PUB_DATE,
                    start, start.plusDays(1), true, true);
            Query q = QueryUtils.addQueryToQuery(topicQuery, numQuery);
            
            int articlesTotal = is.search(q, Integer.MAX_VALUE).totalHits;
            //System.out.println(DateTimeUtils.simpleDateTimeFormatter.print(start));
            //System.out.println("" + articlesTotal);
            csv.writeNext(new String[]{DateTimeUtils.simpleDateTimeFormatter.print(start), "" + articlesTotal});
            
            start = start.plusDays(1);
        }
        
        ir.close();
        csv.close();
    }
    
    public static void main(String[] args) {
        try {
            new TopicFrequencyStats().run();
        } catch (Exception ex) {
            Logger.getLogger(TopicFrequencyStats.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
