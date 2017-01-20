/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import java.io.IOException;
import java.util.logging.Logger;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * This class should add Sentiment to a sentence. TODO: currently we cannot
 * split into sentences.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class SentenceSentimentTaggingFilter extends TokenFilter {

    private static final Logger LOG = Logger.getLogger(SentenceSentimentTaggingFilter.class.getSimpleName());

    SentimentAttribute sentimentAttribute = addAttribute(SentimentAttribute.class);
    CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);

    private SentimentAnalyzer sentimentAnalyzer;

    public SentenceSentimentTaggingFilter(
            SentimentAnalyzer sentimentAnalyzer,
            boolean myMethod,
            TokenStream input) {
        super(input);
        this.sentimentAnalyzer = sentimentAnalyzer;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }
        sentimentAttribute.setSentenceSentiment(determineSentenceSentiment(termAttribute.buffer(), 0, termAttribute.length()));
        return true;
    }

    protected Sentiment determineSentenceSentiment(char[] term, int offset, int length) {
        final String sentence = new String(term, offset, length);
        return sentimentAnalyzer.calcSentiment(sentence);
    }
}
