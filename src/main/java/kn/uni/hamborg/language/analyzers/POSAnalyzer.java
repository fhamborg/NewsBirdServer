/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.PosConfig;

/**
 * Stanford CoreNLP based.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class POSAnalyzer {

    private static final Logger LOG = Logger.getLogger(POSAnalyzer.class.getSimpleName());

    private final Tokenizer tokenizer;
    private final MaxentTagger tagger;

    public POSAnalyzer() {
        tagger = new MaxentTagger(PosConfig.modelEnglishPOSTagger.getAbsolutePath());
        tokenizer = new Tokenizer();
    }

    public List<CoreLabel> tokenize(String sentence) {
        return tokenizer.tokenizeText(sentence);
    }

    public List<CoreLabel> tagSentence(String sentence) {
        List<CoreLabel> tokens = tokenize(sentence);
        try {
            tagger.tagCoreLabels(tokens);
        } catch (OutOfMemoryError oome) {
            LOG.log(Level.WARNING, "skipping sentence because of heap error: {0}...", sentence.substring(0, Math.min(sentence.length(), 30)));
        }
        /*    for (CoreLabel token : tokens) {
         //System.out.println(token.toString());
         String pos = CoreLabelUtils.getPos(token);
         SimplePos simplePos = CoreLabelUtils.getSimplePosFromCoreLabelPos(pos);
         //  System.out.println(pos + " - " + simplePos);

         }*/
        return tokens;
    }

    public static void main(String[] args) throws FileNotFoundException {
        POSAnalyzer p = new POSAnalyzer();
        p.tagSentence("Hi my name is Felix Hamborg. How are you? Nice man!");
    }
}
