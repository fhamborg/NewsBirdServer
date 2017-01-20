/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.lucene.summarizer;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import kn.uni.hamborg.utils.ObjectScore;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class StringScore extends ObjectScore {

    private static final Logger LOG = Logger.getLogger(StringScore.class.getSimpleName());

    private final String value;

    public StringScore(String value, double score) {
        super(score);
        this.value = value;

    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Only checks whether the other StringScore has the same value. Does not
     * check the score.
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        return value.equals(((StringScore) obj).value);
    }

    public static String[] valueArray(List<StringScore> list) {
        return valueArray(list.toArray(new StringScore[0]));
    }

    public static String[] valueArray(StringScore[] array) {
        String[] values = new String[array.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = array[i].getValue();
        }
        return values;
    }

    public static List<String> valueList(List<StringScore> list) {
        return Arrays.asList(valueArray(list));
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
