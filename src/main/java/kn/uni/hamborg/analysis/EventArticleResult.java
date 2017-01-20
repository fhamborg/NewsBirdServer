/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.analysis;

import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.joda.time.DateTime;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EventArticleResult {

    private static final Logger LOG = Logger.getLogger(EventArticleResult.class.getSimpleName());

    private Document[] eventArticleDocuments;
    private DateTime mainDate;
    private String query;

    public EventArticleResult(Document[] eventArticleDocuments, DateTime mainDate, String query) {
        this.eventArticleDocuments = eventArticleDocuments;
        this.mainDate = mainDate;
        this.query = query;
    }

    public Document[] getEventArticleDocuments() {
        return eventArticleDocuments;
    }

    public DateTime getMainDate() {
        return mainDate;
    }

    public String getQuery() {
        return query;
    }

    public void setEventArticleDocuments(Document[] eventArticleDocuments) {
        this.eventArticleDocuments = eventArticleDocuments;
    }

    public void setMainDate(DateTime mainDate) {
        this.mainDate = mainDate;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
