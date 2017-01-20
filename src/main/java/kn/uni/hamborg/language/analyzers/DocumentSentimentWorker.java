/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import java.util.List;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;

/**
 * A worker class to compute the sentiment of a given {@link Document} and its
 * {@code fieldname}
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class DocumentSentimentWorker implements Runnable {

    private static final Logger LOG = Logger.getLogger(DocumentSentimentWorker.class.getSimpleName());

    private String fieldname;
    private Document doc;
    private SentimentAnalyzer sentimentAnalyzer;
    private List<DocumentSentiment> resultList;

    public DocumentSentimentWorker(Document doc, String fieldname,
            SentimentAnalyzer sentimentAnalyzer, List<DocumentSentiment> resultList) {
        this.fieldname = fieldname;
        this.doc = doc;
        this.sentimentAnalyzer = sentimentAnalyzer;
        this.resultList = resultList;
    }

    @Override
    public void run() {
        resultList.add(sentimentAnalyzer.calcSentiment(doc, fieldname));
        System.out.print(".");
    }

}
