/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import edu.berkeley.nlp.lm.StringWordIndexer;
import edu.berkeley.nlp.lm.WordIndexer;
import edu.berkeley.nlp.lm.io.KneserNeyLmReaderCallback;
import edu.berkeley.nlp.lm.util.LongRef;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import opennlp.tools.tokenize.SimpleTokenizer;

/**
 * Courtesy by Florian Stoffel. Provides functionality for a n-gram model for
 * probability calculation.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class NGramModel {

    private final WordIndexer<String> indexer;
    private final KneserNeyLmReaderCallback<String> model;
    private final SentenceSplitter sentenceSplitter;
    private final int n;

    private NGramModel(int n) {
        if (n <= 1) {
            throw new IllegalArgumentException("n must be larger than one");
        }

        this.indexer = new StringWordIndexer();
        this.indexer.setStartSymbol("<s>");
        this.indexer.setEndSymbol("</s>");
        this.indexer.setUnkSymbol("<unk>");

        this.model = new KneserNeyLmReaderCallback<>(this.indexer, n);
        this.n = n;
        try {
            this.sentenceSplitter = new SentenceSplitter();
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    /**
     * Indexes the given text. Therefore, at first the text is split into
     * sentences which are then tokenized.
     *
     * @param text
     */
    public void indexText(String text) {
        String[] sentences = sentenceSplitter.splitSentences(text);
        for (String sentence : sentences) {
            //System.out.println("indexing sentence: " + sentence);
            String[] tokens = SimpleTokenizer.INSTANCE.tokenize(sentence);
            indexSentence(tokens);
        }

    }
    static int count = 0;

    /**
     * Indexes the given sentence
     *
     * @param sentence the sentence words
     */
    public void indexSentence(String... sentence) {
        List<String> tokens = new ArrayList<>(Arrays.asList(sentence));
        tokens.add(0, indexer.getStartSymbol());
        tokens.add(tokens.size(), indexer.getEndSymbol());

        for (int i = 0; i <= tokens.size() - n; i++) {
            this.indexNgram(tokens.subList(i, i + n));
        }
    }

    /**
     * Indexes this n gram
     *
     * @param ngram
     */
    public synchronized void indexNgram(String[] ngram) {
        if (ngram.length != n) {
            throw new RuntimeException("ngram length is != " + n);
        }

        //System.out.println("indexing: " + Arrays.asList(ngram));

        int[] tmp = new int[ngram.length];
        for (int i = 0; i < ngram.length; ++i) {
            tmp[i] = this.indexer.getOrAddIndexFromString(ngram[i]);
        }

        this.model.call(tmp, 0, ngram.length, new LongRef(count++), join(ngram, 0, ngram.length));
    }

    /**
     * Indexes this n gram
     *
     * @param ngram
     */
    public void indexNgram(List<String> ngram) {
        indexNgram(ngram.toArray(new String[0]));
    }

    /**
     * Returns the score of the given n-gram
     *
     * @param ngram the n-gram to score
     * @return the score of the given n-gram
     */
    public float getScore(String... ngram) {
        return this.model.getLogProb(Arrays.asList(ngram));
    }

    /**
     * Returns the score for a given sentence
     *
     * @param sentence the sentence to score
     * @return the score for a given sentence
     */
    public float getSentenceScore(String... sentence) {
        List<String> tokens = new ArrayList<>(Arrays.asList(sentence));
        return this.model.scoreSentence(tokens);
    }

    public float getTextScore(String text) {
        String[] sentences = sentenceSplitter.splitSentences(text);
        float score = 0.0f;
        for (String sentence : sentences) {
            score += getSentenceScore(sentence);
        }
        return score;
    }

    /**
     * Returns the score for a given sentence. The sentence is tokenized with a
     * simple character based tokenizer.
     *
     * @param sentence the sentence to score
     * @return the score for a given sentence
     */
    public float getSentenceScore(String sentence) {
        return this.getSentenceScore(SimpleTokenizer.INSTANCE.tokenize(sentence));
    }

    private static String join(String[] words, int start, int length) {
        StringBuilder result = new StringBuilder();
        for (int i = start; i < start + length; ++i) {
            result.append(words[i]);
            if (i < start + length - 1) {
                result.append(' ');
            }
        }

        return result.toString();
    }

    /**
     * Returns a new bi-gram model
     *
     * @return a new bi-gram model
     */
    public static NGramModel createBiGramModel() {
        return new NGramModel(2);
    }

    /**
     * Returns a new tri-gram model
     *
     * @return a new tri-gram model
     */
    public static NGramModel createTriGramModel() {
        return new NGramModel(3);
    }

    /**
     * Returns a new n-gram model
     *
     * @param n the number of tokens (<b>n</b>-gram)
     * @return a new n-gram model
     */
    public static NGramModel createNGramModel(int n) {
        return new NGramModel(n);
    }
}
