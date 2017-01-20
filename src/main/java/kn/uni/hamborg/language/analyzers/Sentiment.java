/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public enum Sentiment {

    GOOD(1), NEUTRAL(0), NEGATIVE(-1);

    private int value;

    Sentiment(int value) {
        this.value = value;
    }

    /**
     * Returns this sentiment's numerical representation.
     *
     * @return
     */
    public int getAsNumber() {
        return value;
    }

    public static Sentiment createFromNumber(int value) {
        for (Sentiment s : Sentiment.values()) {
            if (s.getAsNumber() == value) {
                return s;
            }
        }
        return null;
    }

}
