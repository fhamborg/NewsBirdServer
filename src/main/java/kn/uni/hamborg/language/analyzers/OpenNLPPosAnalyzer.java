/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.OpenNLPConfig;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

/**
 * OpenNLP based POS Tagger.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class OpenNLPPosAnalyzer {

    private static final Logger LOG = Logger.getLogger(OpenNLPPosAnalyzer.class.getSimpleName());

    private final OpenNLPTokenizer tokenizer;
    private final POSTaggerME posTagger;

    public OpenNLPPosAnalyzer(OpenNLPTokenizer tokenizer) {
        this.tokenizer = tokenizer;
        try {
            posTagger = new POSTaggerME(new POSModel(new File(OpenNLPConfig.basePath,
                    "en" + "-pos-maxent.bin")));

        } catch (IOException ex) {
            Logger.getLogger(OpenNLPPosAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public OpenNLPPosAnalyzer() {
        this(new OpenNLPTokenizer());
    }

    /**
     * Slow. Tokenizes the sentence at first and then returns both POS tags.
     *
     * @param sentence
     * @return
     */
    public String[] tagSentence(String sentence) {
        return tagSentence(tokenizer.tokenizeSentence(sentence));
    }

    public PosInf tagSentenceToInfo(String sentence) {
        final String[] tokens = tokenizer.tokenizeSentence(sentence);

        return new PosInf(tokens, tagSentence(tokens));
    }

    public OpenNLPTokenizer getTokenizer() {
        return tokenizer;
    }

    /**
     * Faster. Returns POS tags, one for each token from {@code tokens}.
     *
     * @param tokens
     * @return
     */
    public String[] tagSentence(String[] tokens) {
        return posTagger.tag(tokens);
    }

    public static void main(String[] args) throws IOException {
        OpenNLPPosAnalyzer a = new OpenNLPPosAnalyzer();

        /*   System.out.println(Arrays.toString(a.tagSentence("Putin")));
         System.out.println(Arrays.toString(a.tagSentence("Felix")));
         System.out.println(Arrays.toString(a.tagSentence("felix")));
         System.out.println(Arrays.toString(a.tagSentence("putin")));
         System.out.println(Arrays.toString(a.tagSentence("did")));
         System.out.println(Arrays.toString(a.tagSentence("he")));
         System.out.println(Arrays.toString(a.tagSentence("said")));
         System.out.println(Arrays.toString(a.tagSentence("sai")));
         */
        System.out.println(Arrays.toString(a.tagSentence("\"But this is shit.\"")));
        System.out.println(Arrays.toString(a.tagSentence("'However this is shit.'")));
        System.out.println(Arrays.toString(a.tagSentence("Because this is shit.")));
        System.out.println(Arrays.toString(a.tagSentence("Altough this is shit.")));
        System.out.println(Arrays.toString(a.tagSentence("If this is shit, I will be glad.")));
        System.out.println(Arrays.toString(a.tagSentence("He is here.")));
        System.out.println(Arrays.toString(a.tagSentence("They are cool.")));
        System.out.println(Arrays.toString(a.tagSentence("She said that he would go there.")));

    }

    public static class PosInf {

        public PosInf(String[] tokens, String[] postags) {
            this.tokens = tokens;
            this.postags = postags;
        }

        public final String[] tokens;
        public final String[] postags;
    }
}
