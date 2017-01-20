/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.summary;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import kn.uni.hamborg.utils.NumberUtils;

/**
 * Represents a summary, mainly consisting of sentences that sum up the
 * underlying text.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class Summary {

    private static final Logger LOG = Logger.getLogger(Summary.class.getSimpleName());

    private final Map<String, Set<String>> topSentencesToLightDocIds;
    private final StringScore[] topSentences;
    private final StringScore[] topTerms;
    private final StringScore[] topTermsOfTopics;

    public Summary(StringScore[] topSentences, StringScore[] topTerms, StringScore[] topTermsOfTopics) {
        this(topSentences, topTerms, topTermsOfTopics, null);
    }

    public Summary(StringScore[] topSentences, StringScore[] topTerms, StringScore[] topTermsOfTopics,
            Map<String, Set<String>> topSentencesToLightDocIds) {
        this.topSentences = topSentences;
        this.topTerms = topTerms;
        this.topTermsOfTopics = topTermsOfTopics;
        this.topSentencesToLightDocIds = topSentencesToLightDocIds;
    }

    public StringScore[] getTopSentences() {
        return topSentences;
    }

    public StringScore[] getTopTerms() {
        return topTerms;
    }

    public StringScore[] getTopTermsOfTopics() {
        return topTermsOfTopics;
    }

    public Map<String, Set<String>> getTopSentencesToLightDocIds() {
        return topSentencesToLightDocIds;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (StringScore topSentence : topSentences) {
            sb
                    .append(NumberUtils.detailedDecimalFormat.format(topSentence.getScore()))
                    .append(topSentence.getValue())
                    .append(" ");
        }
        return sb.toString();
    }

}
