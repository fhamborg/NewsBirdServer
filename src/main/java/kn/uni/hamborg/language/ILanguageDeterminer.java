package kn.uni.hamborg.language;

/**
 * Interface for an Language-Determiner. At the moment only the languages
 * English and German are supported.
 *
 * @author Michael Hund, University of Konstanz
 *
 */
public interface ILanguageDeterminer {

    /**
     * Return true, if the sentence contain at least one non-latin character.
     * This method can be used for an easy filtering step.
     *
     * @param sentence
     * @return
     */
    public boolean containsNonLatinCharacters(final String sentence);

    /**
     * Return true, if the given sentence is a English sentence, otherwise false
     *
     * @param sentenceTokens tokens of the sentence
     * @param sentence whole sentence in one string
     * @return
     */
    public boolean isEnglishSentence(final String[] sentenceTokens, final String sentence);

    /**
     * Return true, if the given sentence is a German sentence, otherwise false
     *
     * @param sentenceTokens tokens of the sentence
     * @param sentence whole sentence in one string
     * @return
     */
    public boolean isGermanSentence(final String[] sentenceTokens, final String sentence);

}
