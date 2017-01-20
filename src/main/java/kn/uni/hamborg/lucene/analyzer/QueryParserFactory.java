/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.lucene.analyzer;

import java.util.logging.Logger;
import kn.uni.hamborg.data.light.LightDoc;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class QueryParserFactory {

    private static final Logger LOG = Logger.getLogger(QueryParserFactory.class.getSimpleName());

    public static final String defaultFieldNameContent = LightDoc.CONTENT_STEMMED;

    public static QueryParser createQueryParser() {
        return createQueryParser(AnalyzerFactory.createCustomAnalyzer());
    }

    public static QueryParser createQueryParser(String fieldname) {
        return new DefaultQueryParser(fieldname, AnalyzerFactory.createCustomAnalyzer());
    }

    public static QueryParser createQueryParser(Analyzer analyzer) {
        return new DefaultQueryParser(defaultFieldNameContent, analyzer);
    }
}
