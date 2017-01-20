/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.scorer;

import java.util.List;
import java.util.logging.Logger;
import kn.uni.hamborg.lucene.summarizer.StringScore;

/**
 * This represents scoring information, e.g., precomputed token scores or phrase
 * scores. A ScoringInformation object is supposed to be displayed, i.e., the
 * elements are already in order and just need to be visualized correspondingly.
 * One element coult be a token, a phrase, a sentence, etc.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ScoringInformation {

    private static final Logger LOG = Logger.getLogger(ScoringInformation.class.getSimpleName());

    private final double minScore;
    private final double maxScore;
    private final List<StringScore> elementScores;

    public ScoringInformation(double minScore, double maxScore, List<StringScore> elementScores) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.elementScores = elementScores;
    }

}
