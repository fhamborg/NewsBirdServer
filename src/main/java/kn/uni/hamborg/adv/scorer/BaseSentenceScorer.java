/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.scorer;

import java.util.logging.Logger;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public abstract class BaseSentenceScorer {

    private static final Logger LOG = Logger.getLogger(BaseSentenceScorer.class.getSimpleName());

    protected static final double MINUS = -0.5;
    protected static final double MINUS_DOUBLE = MINUS * 2;
    protected static final double DISCARD = -Double.MAX_VALUE * 0.00000000001; // we do not want to run into infinity conversion problems (JSON), so we do not
    // use numbers which could result in infinity

    /**
     * Computes a relative score of this sentence. For no change this is 0, to
     * decrease the score this returns a number <0.
     *
     * @param tokens
     * @param pos
     * @return
     */
    public abstract double computeRelativeScoreOfSentence(String[] tokens, String[] pos);

}
