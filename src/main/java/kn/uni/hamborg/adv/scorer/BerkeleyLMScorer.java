/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.scorer;

import java.util.logging.Logger;
import kn.uni.hamborg.language.analyzers.NGramModel;

/**
 * Calculates the probability of a sentence for a corpus file. Should be used to
 * find out whether a sentence in a cell is likely to occur (and thus maybe not
 * particularly interesting) in the whole matrix.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class BerkeleyLMScorer {

    private static final Logger LOG = Logger.getLogger(BerkeleyLMScorer.class.getSimpleName());

    // n-grams number
    private static final int N = 3;

    private final NGramModel ngramModel;

    public BerkeleyLMScorer() {
        ngramModel = NGramModel.createNGramModel(N);
    }

    public BerkeleyLMScorer(String corpusText) {
        ngramModel = NGramModel.createNGramModel(N);
        indexText(corpusText);
    }

    public final void indexText(String text) {
        ngramModel.indexText(text);
    }

    public float getTextScore(String text) {
        return ngramModel.getTextScore(text);
    }

    public float getSentenceProb(String sentence) {
        return ngramModel.getSentenceScore(sentence);
    }

    public float getProb(String[] s) {
        return ngramModel.getScore(s);
    }

    public static void main(String[] args) {
        BerkeleyLMScorer lms = new BerkeleyLMScorer("Interest rates kept on hold amid signs that UK's... Interest rates have been left on hold at 0.5% for another month amid fears that the pace of... guten abend gute nacht. mein name ist felix.");
        System.out.println(lms.getSentenceProb("Interest rates kept on hold amid signs that UK's... Interest rates have been left on hold at 0.5% for another month amid fears that the pace of..."));
        System.out.println(lms.getTextScore("Video: Widow of slain ‘African Che Guevara’ seeks answers"));
        System.out.println(lms.getTextScore("guten abend gute nacht. mein name ist felix."));

        /*     System.out.println(lms.getProb(new String[]{"guten", "abend"}));
         System.out.println(lms.getProb(new String[]{"guten", "abend", "gute"}));
         System.out.println(lms.getProb(new String[]{"guten", "abend", "nacht"}));*/
    }
}
