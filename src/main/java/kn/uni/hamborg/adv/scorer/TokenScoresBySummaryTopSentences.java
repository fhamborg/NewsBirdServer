/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.scorer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import kn.uni.hamborg.lucene.summarizer.StringScore;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TokenScoresBySummaryTopSentences {

    private static final Logger LOG = Logger.getLogger(TokenScoresBySummaryTopSentences.class.getSimpleName());

    private final List<StringScore> tokenScores;
    /**
     * Just for faster access.
     */
    private transient final Map<String, Double> tokenScoresMap;

    public TokenScoresBySummaryTopSentences(List<StringScore> tokenScores) {
        this.tokenScores = tokenScores;
        tokenScoresMap = new HashMap<>();
        for (StringScore tokenScore : tokenScores) {
            tokenScoresMap.put(tokenScore.getValue(), tokenScore.getScore());
        }
    }

    public List<StringScore> getTokenScores() {
        return tokenScores;
    }

    public Map<String, Double> getTokenScoresMap() {
        return tokenScoresMap;
    }

}
