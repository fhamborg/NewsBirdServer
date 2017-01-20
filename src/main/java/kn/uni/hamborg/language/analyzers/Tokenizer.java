/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provides tokenization (text into tokens). Based on
 * http://nlp.stanford.edu/software/tokenizer.shtml
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class Tokenizer {

    private static final Logger LOG = Logger.getLogger(Tokenizer.class.getSimpleName());

    /**
     * Tokenizes text into its tokens.
     *
     * @param text
     * @return
     */
    public List<CoreLabel> tokenizeText(String text) {
        PTBTokenizer ptbt = new PTBTokenizer(new StringReader(text),
                new CoreLabelTokenFactory(), "");
        List<CoreLabel> tokens = new ArrayList<>();
        for (CoreLabel label;
                ptbt.hasNext();) {
            label = (CoreLabel) ptbt.next();
            tokens.add(label);
        }
        return tokens;
    }
}
