/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import kn.uni.hamborg.lucene.summarizer.StringScore;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class StringScoreUtils {

    private static final Logger LOG = Logger.getLogger(StringScoreUtils.class.getSimpleName());

    /**
     * Creates a new List which contains the same values from the scores but
     * with a fixed score defined by score.
     *
     * @param scores
     * @param score
     * @return
     */
    public static List<StringScore> withAllScoresTo(List<StringScore> scores, double score) {
        List<StringScore> newScores = new ArrayList<>();
        for (StringScore tmp : scores) {
            newScores.add(new StringScore(tmp.getValue(), score));
        }
        return newScores;
    }

}
