/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class LuceneUtils {

    private static final Logger LOG = Logger.getLogger(LuceneUtils.class.getSimpleName());

    public static List<String> parseKeywords(Analyzer analyzer, String field, String keywords) {
        List<String> result = new ArrayList<>();
        try {
            TokenStream stream = analyzer.tokenStream(field, new StringReader(keywords));
            stream.reset();
            while (stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
            stream.close();
        } catch (IOException e) {
            // not thrown b/c we're using a string reader...
        }

        return result;

    }
}
