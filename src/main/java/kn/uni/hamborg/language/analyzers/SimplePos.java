/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

/**
 * The pos tags in here are based on the MPQA lexicon. See
 * {@link MPQASubjectivityExtractor}. They also are written exactly alike.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public enum SimplePos {

    noun, adjective, verb, adverb, anypos,
    /**
     * This POS tag is used for any other POS other than those above.
     */
    other;

    public static SimplePos fromMPQAString(String mpqaPos) {
        switch (mpqaPos) {
            case "adj":
                return adjective;
            case "anypos":
                return anypos;
            default:
                return SimplePos.valueOf(mpqaPos);
        }
        // return other;
    }
}
