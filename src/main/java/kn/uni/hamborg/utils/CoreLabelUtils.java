/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import java.util.logging.Logger;
import kn.uni.hamborg.language.analyzers.SimplePos;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CoreLabelUtils {

    private static final Logger LOG = Logger.getLogger(CoreLabelUtils.class.getSimpleName());

    public static String getPos(CoreLabel coreLabel) {
        return coreLabel.get(CoreAnnotations.PartOfSpeechAnnotation.class);
    }

    public static String getToken(CoreLabel coreLabel) {
        /*    System.out.println(coreLabel.get(CoreAnnotations.TextAnnotation.class));
         System.out.println(coreLabel.get(CoreAnnotations.OriginalTextAnnotation.class));
         System.out.println(coreLabel.get(CoreAnnotations.ValueAnnotation.class));
         */
        return coreLabel.get(CoreAnnotations.TextAnnotation.class);
    }

    /**
     * Convert according to
     * https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
     *
     * @param coreLabelPos
     * @return
     */
    public static SimplePos getSimplePosFromCoreLabelPos(String coreLabelPos) {
        if (coreLabelPos.startsWith("NN")) {
            return SimplePos.noun;
        } else if (coreLabelPos.startsWith("JJ")) {
            return SimplePos.adjective;
        } else if (coreLabelPos.startsWith("VB")) {
            return SimplePos.verb;
        } else if (coreLabelPos.startsWith("RB")) {
            return SimplePos.adverb;
        }

        // System.out.println(coreLabelPos);
        return SimplePos.other;
    }

}
