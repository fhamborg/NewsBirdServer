/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.stopwords;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Globally valid stop words.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class WorldStopWords {

    private static final Logger LOG = Logger.getLogger(WorldStopWords.class.getSimpleName());

    // see 150407_world stop words
    public static final String[] STOPWORDS = new String[]{
        "said", "he", "from", "have", "has", "his", "we", "i", "who",
        "were", "been", "had", "which", "its", "more", "also", "year",
        "after", "one", "about", "new", "all", "up", "would", "you",
        "people", "when", "out", "two", "she", "her", "over", "than",
        "can", "other", "our", "time", "first", "2014", "2015", "2013",
        "2011", "2010", "2016", "last", "so", "some", "state", "what",
        "years", "us", "them", "could", "while", "now", "only", "just",
        "three", "against", "do", "may", "like", "being", "any", "world",
        "most", "where", "before", "him", "because", "percent", "day",
        "during", "told", "through", "since", "many", "those", "my",
        "well", "including", "made", "how", "mr", "minister", "city",
        "says", "should", "u.s", "u.s.a.", "between", "under", "back",
        "group", "get", "according", "national", "1", "your", "country"};

    public static final ImmutableSet<String> STOPWORDSET = ImmutableSet.copyOf(Arrays.asList(STOPWORDS));

    /**
     * Returns true if the token (which needs to be unstemmed but lowercased) is
     * a stopword.
     *
     * @param token
     * @return
     */
    public static boolean isStopWord(String token) {
        return ArrayUtils.contains(STOPWORDS, token);
    }
}
