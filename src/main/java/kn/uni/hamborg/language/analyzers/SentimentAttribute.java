/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import java.util.logging.Logger;
import org.apache.lucene.util.AttributeImpl;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class SentimentAttribute extends AttributeImpl {

    private static final Logger LOG = Logger.getLogger(SentimentAttribute.class.getSimpleName());

    private Sentiment sentenceSentiment = Sentiment.NEUTRAL;

    public void setSentenceSentiment(Sentiment sentenceSentiment) {
        this.sentenceSentiment = sentenceSentiment;
    }

    public Sentiment getSentenceSentiment() {
        return sentenceSentiment;
    }

    @Override
    public void clear() {
        sentenceSentiment = Sentiment.NEUTRAL;
    }

    @Override
    public void copyTo(AttributeImpl target) {
        ((SentimentAttribute) target).setSentenceSentiment(sentenceSentiment);
    }

}
