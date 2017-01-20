/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.scorer;

import java.util.logging.Logger;
import kn.uni.hamborg.adv.table.FilterCell;

/**
 * Scores sentences based on characteristics described in
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class FelixScorer extends BaseSentenceScorer {

    private static final Logger LOG = Logger.getLogger(FelixScorer.class.getSimpleName());

    private static final int SENTENCE_MIN_CHAR_LENGTH = 30;

    /**
     *
     * @param tokens
     * @param pos can be {@code null} as we do not use it currently.
     * @return
     */
    @Override
    public double computeRelativeScoreOfSentence(String[] tokens, String[] pos) {
        double sentenceScore = 0.0;
        // get for token and POS, this handles the stigma words mentioned in 2.2.2
        sentenceScore += getScoreForSentence(tokens);

        return sentenceScore;
    }

    private double getScoreForSentence(String[] tokens) {
        double score = 0.0;
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            sb.append(token).append(" ");
        }
        String sentence = sb.toString().trim();

        // sentence length
        if (sentence.length() < SENTENCE_MIN_CHAR_LENGTH) {
            System.out.println("found [sentence too few chars = " + sentence.length() + "]");
            score += MINUS_DOUBLE;
        }

        return score;
    }

    /**
     * Computes the score for a given token
     *
     * @param cell
     * @param token
     * @return
     */
    public double computeRelativeScoreOfToken(FilterCell cell, String token) {
        double score = 0.0;

        final String lowerToken = token.toLowerCase();
        if (lowerToken.equals(cell.getRowValue().toLowerCase())
                || lowerToken.equals(cell.getColumnValue().toLowerCase())) {
            System.out.println("found " + lowerToken + " [equals row/column value]");
            score += DISCARD;
        }

        return score;
    }
}
