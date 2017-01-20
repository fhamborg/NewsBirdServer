/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.lucene.summarizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import kn.uni.hamborg.language.analyzers.OpenNLPChunkerAnalyzer;

/**
 * Extracts the (hopefully) most important chunk / phrase from a sentence.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ChunkExtractor {

    private static final Logger LOG = Logger.getLogger(ChunkExtractor.class.getSimpleName());
    private final OpenNLPChunkerAnalyzer chunkerAnalyzer;

    public ChunkExtractor() {
        try {
            this.chunkerAnalyzer = new OpenNLPChunkerAnalyzer();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String[] extractMostImportantChunks(String sentence) {
        String[] tokens = chunkerAnalyzer.getTokenizer().tokenizeSentence(sentence);
        return extractMostImportantChunks(tokens);
    }

    public String[] extractMostImportantChunks(String[] tokens) {
        String[] pos = chunkerAnalyzer.getPosTagger().tagSentence(tokens);
        return extractMostImportantChunks(tokens, pos);
    }

    public String[] extractMostImportantChunks(String[] tokens, String[] pos) {
        String[] chunks = chunkerAnalyzer.chunkSentence(tokens, pos);

        List<String> topTokens = new ArrayList<>();

        /**
         * The idea is that in English sentences / language, the subject is the
         * first part of the sentence (the first noun phrase). The verb phrase
         * follows, consisting of a verb. This is followed by another noun
         * phrase, the object. The following processes the chunks with this idea
         * and cuts off immediately after the second noun phrase, i.e., the
         * object, ends.
         */
        int state = 0;
        outerloop:
        for (int i = 0; i < chunks.length; i++) {
            String chunkPos = OpenNLPChunkerAnalyzer.getChunkPos(chunks[i]);
            switch (chunkPos) {
                case "NP":
                    if (state == 0) {
                        state++;
                    } else if (state == 2) {
                        state++;
                    }
                    break;
                case "VP":
                    if (state == 1) {
                        state++;
                    }
                    break;
                default:
                    if (state == 3) {
                        break outerloop;
                    }
            }
            topTokens.add(tokens[i]);
        }
        topTokens.add(".");

        return topTokens.toArray(new String[0]);
    }

    public static void main(String[] args) {
        ChunkExtractor ce = new ChunkExtractor();
        String[] sentences = new String[]{
            "Ukraine and the West have continuously accused Moscow of fuelling a pro-Russian rebellion in the east with troops and weapons, which Russia has denied.",
            "Ukraine was hit by a \"substantial rollback\" and faced a further escalation of violence, President Petro Poroshenko said on Friday after hearing reports from his security command that a heavily armored unit had crossed into Ukraine from Russia.",
            "Russia has, however, denied the allegations while terming it as malicious and provocative."
        };

        for (String sentence : sentences) {
            System.out.println(Arrays.toString(ce.extractMostImportantChunks(sentence)));
        }
    }
}
