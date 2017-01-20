/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.scorer;

import java.util.logging.Logger;
import kn.uni.hamborg.language.StigmaWords;
import kn.uni.hamborg.language.analyzers.OpenNLPParser;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Uses {@link OpenNLPParser} to parse tokens or sentences or documents. We
 * score according to Lin2002Single.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class SentenceLin2002SingleScorer extends BaseSentenceScorer {

    private static final Logger LOG = Logger.getLogger(SentenceLin2002SingleScorer.class.getSimpleName());

    /**
     * Gets the relative score.
     *
     * @param token
     * @param postag
     * @return
     */
    private double getScoreForFirstToken(String token, String postag) {
        double score = 0.0;
        String lowerToken = token.toLowerCase();
        // conjunctions
        if (ArrayUtils.contains(StigmaWords.CONJUNCTION_POS, postag)
                || ArrayUtils.contains(StigmaWords.CONJUNTION_TOKENS, lowerToken)) {
            System.out.println("found " + token + " [conjunction] " + postag);
            score += MINUS;
        }

        // the verb say and its derivatives
        if (ArrayUtils.contains(StigmaWords.SAY_DERIVATIVES, lowerToken)) {
            System.out.println("found " + token + " [say] " + postag);
            score += MINUS;
        }
        // quotation marks
        if (ArrayUtils.contains(StigmaWords.QUOTES, lowerToken.substring(0, 1))
                || ArrayUtils.contains(StigmaWords.QUOTES_POS, postag)) {
            System.out.println("found " + token + " [quotation mark] ");
            score += DISCARD;
        }
        // pronouns
        if (postag.equals(StigmaWords.PRONOUNS)) {
            System.out.println("found " + token + " [pronouns] " + postag);
            score += MINUS;
        }

        return score;
    }

    @Override
    public double computeRelativeScoreOfSentence(String[] tokens, String[] pos) {
        double sentenceScore = 0.0;
        // get for token and POS, this handles the stigma words mentioned in 2.2.2
        sentenceScore += getScoreForFirstToken(tokens[0], pos[0]);

        return sentenceScore;
    }

}
