/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import kn.uni.hamborg.config.OpenNLPConfig;
import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

/**
 * Provides an easy way to work the OpenNLP's chunker.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class OpenNLPChunkerAnalyzer {

    private static final Logger LOG = Logger.getLogger(OpenNLPChunkerAnalyzer.class.getSimpleName());

    private final Chunker chunker;
    private final OpenNLPPosAnalyzer posTagger;
    private final OpenNLPTokenizer tokenizer;

    public OpenNLPChunkerAnalyzer() throws IOException {
        tokenizer = new OpenNLPTokenizer();
        posTagger = new OpenNLPPosAnalyzer();

        chunker = new ChunkerME(new ChunkerModel(new File(OpenNLPConfig.basePath, "en" + "-chunker.bin")));
    }

    public OpenNLPTokenizer getTokenizer() {
        return tokenizer;
    }

    public OpenNLPPosAnalyzer getPosTagger() {
        return posTagger;
    }

    /**
     *
     * @param sentence
     * @return An array , see http://www.clips.uantwerpen.be/conll2000/chunking/
     */
    public String[] chunkSentence(String sentence) {
        return chunkSentence(tokenizer.tokenizeSentence(sentence));
    }

    public String[] chunkSentence(String[] tokens) {
        return chunkSentence(tokens, posTagger.tagSentence(tokens));
    }

    public String[] chunkSentence(String[] tokens, String[] pos) {
        return chunker.chunk(tokens, pos);
    }

    private static String getChunkType(String chunk) {
        return chunk.split("-")[0];
    }

    public static String getChunkPos(String chunk) {
        if (chunk.length() == 1) {
            return chunk;
        }
        return chunk.split("-")[1];
    }

    public static void main(String[] args) throws IOException {
        OpenNLPChunkerAnalyzer ca = new OpenNLPChunkerAnalyzer();
        String s = "National Security and Defense Council spokesman Andriy Lysenko said "
                + "Friday at least 32 tanks, 16 artillery systems and 30 trucks loaded with fighters and "
                + "ammunition had crossed into eastern Ukraine from Russia.";
        /* s = "Israel deems Iran's potential cooperation with Western countries in "
         + "their fight against Islamic State (IS) a mistake, the "
         + "Israeli Minister of Foreign Affairs said Friday.";*/
        s = "Ukraine and the West have continuously accused Moscow of fuelling a pro-Russian rebellion in the east with troops and weapons, which Russia has denied.";
        String[] tokens = ca.tokenizer.tokenizeSentence(s);
        String[] pos = ca.posTagger.tagSentence(tokens);
        String[] res = ca.chunkSentence(s);

        for (int i = 0; i < res.length; i++) {
            System.out.println(pos[i] + "\t| " + res[i] + ": " + tokens[i]);
        }
        System.out.println("");

        int state = 0;

        outerloop:
        for (int i = 0; i < res.length; i++) {
            String chunkPos = getChunkPos(res[i]);
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
            System.out.print(tokens[i] + " ");
        }
    }
}
