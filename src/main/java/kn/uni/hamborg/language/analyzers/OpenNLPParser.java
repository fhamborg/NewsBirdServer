/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.OpenNLPConfig;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.util.Span;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
 * Note that the constructor of this class takes much time until all OpenNLP
 * models are loaded. Thus, this should be done as early as possible so that the
 * object is already initialized when it's needed.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class OpenNLPParser {

    private static final Logger LOG = Logger.getLogger(OpenNLPParser.class.getSimpleName());

    private final OpenNLPPosAnalyzer posTagger;
    private final Parser parser;
    private final SentenceSplitter sentenceSplitter;

    private static final double scoreNN_ = 0.6;
    private static final double scoreVB_ = 0.6;

    public OpenNLPParser() throws IOException {
        DateTime start = DateTime.now();
        sentenceSplitter = new SentenceSplitter();
        posTagger = new OpenNLPPosAnalyzer();
        parser = ParserFactory.create(new ParserModel(new File(OpenNLPConfig.basePath,
                "en" + "-parser-chunking.bin")));
        LOG.log(Level.INFO, "initalization finished in {0} seconds", Seconds.secondsBetween(start, DateTime.now()).getSeconds());
    }

    public Parse parseSentence(String sentence) {
        //  parser.parse(null)
        final Parse p = new Parse(sentence,
                // a new span covering the entire text
                new Span(0, sentence.length()),
                // the label for the top if an incomplete node
                AbstractBottomUpParser.INC_NODE,
                // the probability of this parse...uhhh...?
                1,
                // the token index of the head of this parse
                0);

        // make sure to initialize the _tokenizer correctly
        final Span[] spans = posTagger.getTokenizer().tokenizeSentenceToSpans(sentence);

        for (int idx = 0; idx < spans.length; idx++) {
            final Span span = spans[idx];
            // flesh out the parse with individual token sub-parses
            p.insert(new Parse(sentence,
                    span,
                    AbstractBottomUpParser.TOK_NODE,
                    0,
                    idx));
        }

        final Parse parseTree = parser.parse(p);
        // parseTree.show();

        return parseTree;
    }

    private void recursiveCalcScoresOfParseChild(Parse parent, Map<String, Double> tokenScores) {
        for (Parse child : parent.getChildren()) {
            Double score = tokenScores.get(child.getCoveredText());
            if (score != null) {
                final String token = child.getCoveredText();
                final String type = child.getType();

                if (type.startsWith("NN")) {
                    score += scoreNN_;
                } else if (type.startsWith("VB")) {
                    score += scoreVB_;
                }
                tokenScores.put(token, score);
            }

            recursiveCalcScoresOfParseChild(child, tokenScores);
        }
    }

    public Map<String, Double> calcScoresInSentences(String text) {
        final Map<String, Double> tokenScores = new HashMap<>();
        final String[] sentences = sentenceSplitter.splitSentences(text);
        final String[] allTokens = posTagger.getTokenizer().tokenizeSentence(text);
        for (String token : allTokens) {
            if (token.length() > 1) {
                tokenScores.put(token, 1.0);
            }
        }

        for (String sentence : sentences) {
            final Parse root = parseSentence(sentence);
            recursiveCalcScoresOfParseChild(root, tokenScores);
        }

        return tokenScores;
    }

    public Map<String, Double> calcScoresInSentence(String sentence) {
        final Parse root = parseSentence(sentence);
        final String[] tokens = posTagger.getTokenizer().tokenizeSentence(sentence);
        final Map<String, Double> tokenScores = new HashMap<>();
        for (String token : tokens) {
            if (token.length() > 1) {
                tokenScores.put(token, 1.0);
            }
        }

        recursiveCalcScoresOfParseChild(root, tokenScores);

        return tokenScores;
    }

    public OpenNLPPosAnalyzer getPosTagger() {
        return posTagger;
    }

    public SentenceSplitter getSentenceSplitter() {
        return sentenceSplitter;
    }

    public static void main(String[] args) throws IOException {
        OpenNLPParser parser = new OpenNLPParser();
        String s = "National Security and Defense Council spokesman Andriy Lysenko said "
                + "Friday at least 32 tanks, 16 artillery systems and 30 trucks loaded with fighters and "
                + "ammunition had crossed into eastern Ukraine from Russia. "
                + "Ukraine and the West have continuously accused Moscow of fueling a pro-Russian "
                + "rebellion in the east with troops and weapons, the accusations that Russia has "
                + "denied.";

        System.out.println(parser.calcScoresInSentences(s));
    }

}
