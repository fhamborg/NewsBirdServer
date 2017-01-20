/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language;

import java.util.logging.Logger;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class StigmaWords {

    private static final Logger LOG = Logger.getLogger(StigmaWords.class.getSimpleName());

    public static final String[] CONJUNCTION_POS = new String[]{
        "CC"
    //,"RB",
    //"IN"
    //problem with RB ad IN is that they are not exclusively conjunctions, e.g., in this room, "in" is tagged IN
    };

    public static final String[] CONJUNTION_TOKENS = new String[]{
        // taken from http://grammar.yourdictionary.com/parts-of-speech/conjunctions/conjunctions.html
        // coordinating conjunctions
        "and",
        "but",
        "or",
        "nor",
        "for",
        "yet",
        "so",
        "however",
        // subordinating conjunctions
        "after",
        "although",
        "as",
        "because",
        "before",
        "even",
        "if",
        "inasmuch",
        "lest",
        "now",
        "once",
        "provided",
        "since",
        "supposing",
        "than",
        "that",
        "though",
        "til",
        "unless",
        "until",
        "when",
        "whenever",
        "where",
        "whereas",
        "wherever",
        "whether",
        "which",
        "while",
        "who",
        "whoever",
        "why"
    };
    /**
     * We might need to add other words here as well, because this contains only
     * say.
     */
    public static final String[] SAY_DERIVATIVES = new String[]{
        "say", "said", "says", "saying"
    };

    public static final String[] QUOTES_POS = new String[]{
        "``", "''"
    };

    public static final String[] QUOTES = new String[]{
        "\"", "'", "“"
    };

    public static final String PRONOUNS = "PRP";
}
