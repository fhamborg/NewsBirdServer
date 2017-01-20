/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.lucene.analyzer;

import java.util.HashMap;
import java.util.Map;
import kn.uni.hamborg.data.light.LightDoc;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * Creates our customized Analyzer.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class AnalyzerFactory {

    /**
     * Creates an Analyzer that we use in our custom analyzer to analyze the
     * stemmed fields.
     *
     * @return
     */
    public static Analyzer createAnalyzerForStemmedText() {
        return new EnglishAnalyzer();
    }

    public static Analyzer createAnalyzerForField(String fieldname) {
        switch (fieldname) {
            case LightDoc.CONTENT_STEMMED:
            case LightDoc.TITLE_STEMMED:
                return createAnalyzerForStemmedText();

            default:
                return null;
        }
    }

    /**
     * Creates and returns our customized {@link Analyzer}.
     *
     * @return
     */
    public static PerFieldAnalyzerWrapper createCustomAnalyzer() {
        Analyzer english = createAnalyzerForStemmedText();
        Analyzer keyword = new KeywordAnalyzer();

        Map<String, Analyzer> fieldAnalyzers = new HashMap<>();
        fieldAnalyzers.put(LightDoc.CONTENT_STEMMED, english);
        fieldAnalyzers.put(LightDoc.TITLE_STEMMED, english);
        fieldAnalyzers.put(LightDoc.PUB_COUNTRY, keyword);
        fieldAnalyzers.put(LightDoc.PUB_DATE, keyword);
        fieldAnalyzers.put(LightDoc.PUB_URL, keyword);
        fieldAnalyzers.put(LightDoc.CHANNEL_FILE, keyword);
        fieldAnalyzers.put(LightDoc.CHANNEL_GUID, keyword);
        fieldAnalyzers.put(LightDoc.CHANNEL_LANGUAGE, keyword);
        fieldAnalyzers.put(LightDoc.CHANNEL_POSITION, keyword);
        return new PerFieldAnalyzerWrapper(
                new StandardAnalyzer(), fieldAnalyzers);
    }
}
