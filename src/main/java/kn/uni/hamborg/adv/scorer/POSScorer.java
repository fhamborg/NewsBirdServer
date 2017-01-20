/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.scorer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import kn.uni.hamborg.language.analyzers.OpenNLPParser;
import kn.uni.hamborg.language.analyzers.OpenNLPPosAnalyzer;
import kn.uni.hamborg.lucene.summarizer.StringScore;

/**
 * Uses {@link OpenNLPParser} to parse tokens or sentences or documents. For
 * each different stemmed token one score is calculated:
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class POSScorer {

    private static final Logger LOG = Logger.getLogger(POSScorer.class.getSimpleName());

    private final OpenNLPPosAnalyzer posAnalyzer;

    public POSScorer() {
        this.posAnalyzer = new OpenNLPPosAnalyzer();
    }

    /**
     * Scores the tokens. Ideally, these are tokenized tokens from a sentence in
     * order to have a good quality POS tagging (which is in turn used for
     * scoring). One element of the tokens list must not be phrase or a complete
     * sentence. Also it must not be stemmed already. In such cases POS tagging
     * will not work properly.
     *
     * @param tokens
     * @return
     */
    public List<StringScore> scoreTokens(List<String> tokens) {
        final String[] postags = posAnalyzer.tagSentence(tokens.toArray(new String[0]));
        final List<StringScore> newScores = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            String pos = postags[i];
            double score = getScoreForPos(pos);
            newScores.add(new StringScore(token, score));
        }

        return newScores;
    }

    protected double getScoreForPos(String pos) {
        if (pos.startsWith("NN")) {
            return 2.0;
        }
        if (pos.startsWith("VB")) {
            return 2.0;
        }

        return 1.0;
    }

}
