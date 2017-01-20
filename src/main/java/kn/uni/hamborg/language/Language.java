/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public enum Language {

    EN, DE, UNKNOWN;

    public static Language from2Chars(String twoChars) {
        if (twoChars == null) {
            return null;
        }

        for (Language l : values()) {
            if (l.toString().toLowerCase().equals(twoChars)) {
                return l;
            }
        }

        return null;
    }

    private static ILanguageDeterminer determiner = new WordFrequencyLanguageDeterminer();

    public static Language guessLanguage(String text) {
        String[] tokens = text.split(" ");
        if (determiner.isEnglishSentence(tokens, text)) {
            return EN;
        }

        if (determiner.isGermanSentence(tokens, text)) {
            return DE;
        }
        return UNKNOWN;
    }

}
