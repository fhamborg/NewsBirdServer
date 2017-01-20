/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.util.logging.Logger;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ObjectScore implements Comparable<ObjectScore> {

    private static final Logger LOG = Logger.getLogger(ObjectScore.class.getSimpleName());

    private final double score;

    public ObjectScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    @Override
    public int compareTo(ObjectScore o) {
        return score < o.score ? -1 : score > o.score ? 1 : 0;
    }

}
