/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import java.util.logging.Logger;
import kn.uni.hamborg.config.LanguageConfig;
import kn.uni.hamborg.utils.LightDocUtils;
import kn.uni.hamborg.utils.NumberUtils;
import org.apache.lucene.document.Document;

/**
 * Represents the sentiment of a document.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class DocumentSentiment implements Comparable<DocumentSentiment> {

    private static final Logger LOG = Logger.getLogger(DocumentSentiment.class.getSimpleName());

    private Sentiment sentiment;
    private float normalizedNumericalValue;
    private Document doc;

    /**
     * Constructs a {@link DocumentSentiment} from a normalized (on document
     * level, i.e., sum of sentiment values divided by number of sentences)
     * numerical sentiment value.
     *
     * @param normalizedNumericalValue
     */
    public DocumentSentiment(Document doc, float normalizedNumericalValue) {
        this.normalizedNumericalValue = normalizedNumericalValue;
        this.doc = doc;

        if (normalizedNumericalValue < 0) {
            sentiment = Sentiment.NEGATIVE;
        } else if (normalizedNumericalValue > 0) {
            sentiment = Sentiment.GOOD;
        } else {
            sentiment = Sentiment.NEUTRAL;
        }
    }

    public Sentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }

    public float getNormalizedNumericalValue() {
        return normalizedNumericalValue;
    }

    public void setNormalizedNumericalValue(float normalizedNumericalValue) {
        this.normalizedNumericalValue = normalizedNumericalValue;
    }

    @Override
    public String toString() {
        return LightDocUtils.getTitle(doc) + " [" + NumberUtils.defaultDecimalFormat.format(getNormalizedNumericalValue()) + "]";
    }

    @Override
    public int hashCode() {
        return (int) (normalizedNumericalValue * 10000) + sentiment.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DocumentSentiment)) {
            return false;
        }

        DocumentSentiment other = (DocumentSentiment) obj;
        return other.normalizedNumericalValue == this.normalizedNumericalValue;
    }

    @Override
    public int compareTo(DocumentSentiment o) {
        if (o == null) {
            return 1;
        }

        if (o.normalizedNumericalValue < normalizedNumericalValue) {
            return -1;
        } else if (o.normalizedNumericalValue > normalizedNumericalValue) {
            return 1;
        } else {
            return 0;
        }
    }
}
