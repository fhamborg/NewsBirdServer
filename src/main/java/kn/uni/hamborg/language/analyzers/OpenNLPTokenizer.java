/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.OpenNLPConfig;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

/**
 * OpenNLP based tokenizer for English.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class OpenNLPTokenizer {

    private static final Logger LOG = Logger.getLogger(OpenNLPTokenizer.class.getSimpleName());

    private final opennlp.tools.tokenize.Tokenizer tokenizer;

    public OpenNLPTokenizer() {
        try {
            tokenizer = new TokenizerME(new TokenizerModel(new File(OpenNLPConfig.basePath,
                    "en" + "-token.bin")));
        } catch (IOException ex) {
            Logger.getLogger(OpenNLPTokenizer.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public String[] tokenizeSentence(String sentence) {
        return tokenizer.tokenize(sentence);
    }

    public Span[] tokenizeSentenceToSpans(String sentence) {
        return tokenizer.tokenizePos(sentence);
    }

}
