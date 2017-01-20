/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

/**
 *
 * See {@link MPQASubjectivityExtractor} for more information about the enums.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class Subjectivity {

    private Polarity polarity;

    public Subjectivity(Polarity polarity) {
        this.polarity = polarity;
    }

    public Polarity getPolarity() {
        return polarity;
    }

    public void setPolarity(Polarity polarity) {
        this.polarity = polarity;
    }

    public enum Type {

        STRONG, WEAK
    };

    public enum Polarity {

        POS, NEG, BOTH, NEUTRAL;

        public Polarity inverse() {
            if (this == POS) {
                return NEG;
            } else if (this == NEG) {
                return POS;
            } else {
                return this;
            }
        }
    };

}
