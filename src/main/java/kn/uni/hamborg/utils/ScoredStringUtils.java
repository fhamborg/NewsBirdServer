/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.util.logging.Logger;
import kn.uni.hamborg.lucene.summarizer.StringScore;

/**
 * Provides convenience functions for working with StringScore objects.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ScoredStringUtils {

    private static final Logger LOG = Logger.getLogger(ScoredStringUtils.class.getSimpleName());

    /**
     * Joins a variable number of StringScore objects by concatenating all of
     * them and putting in between any two a ' ' (empty space).
     *
     * @param strings
     * @return
     */
    public static String join(StringScore... strings) {
        StringBuilder sb = new StringBuilder();
        for (StringScore string : strings) {
            sb.append(string.getValue());
            sb.append(" ");
        }

        return sb.toString();
    }
}
