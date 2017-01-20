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
import kn.uni.hamborg.utils.IndexUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;

/**
 * This measures the frequency of articles for each country over the complete
 * dataset.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CountryFrequencyStats {

    private static final Logger LOG = Logger.getLogger(CountryFrequencyStats.class.getSimpleName());

    private void run() throws Exception {
        IndexReader ir = IndexUtils.createIndexReader(IndexUtils.openDirectory(LuceneConfig.INDEX_DIR_2014_WITH_NON_EN));
        IndexSearcher is = IndexUtils.createIndexSearcher(ir);
        CSVWriter csv = new CSVWriter(new FileWriter("countryarticlesfrequencies.csv"));
        csv.writeNext(new String[]{"CountryCode", "ArticleCount"});

        for (String countryCode : CountryNames.countryCodes) {
            System.out.print(countryCode + "=");
            int articlesTotal = is.search(new TermQuery(new Term(LightDoc.PUB_COUNTRY, countryCode)), Integer.MAX_VALUE).totalHits;
            System.out.println(articlesTotal);
            csv.writeNext(new String[]{countryCode, "" + (articlesTotal / 365.0f)});
        }

        ir.close();
        csv.close();
    }

    public static void main(String[] args) {
        try {
            new CountryFrequencyStats().run();
        } catch (Exception ex) {
            Logger.getLogger(CountryFrequencyStats.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
