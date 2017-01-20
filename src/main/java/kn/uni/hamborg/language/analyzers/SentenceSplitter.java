/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.config.OpenNLPConfig;
import kn.uni.hamborg.language.Language;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

/**
 * Provides functionality to detect and split sentences in a text.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class SentenceSplitter {

    private static final Logger LOG = Logger.getLogger(SentenceSplitter.class.getSimpleName());

    private final SentenceDetector sentenceDetector;

    public SentenceSplitter(final Language language) throws IOException {
        String lang = language.toString().toLowerCase();
        sentenceDetector = new SentenceDetectorME(new SentenceModel(new File(OpenNLPConfig.basePath,
                lang + "-sent.bin"
        )));
    }

    /**
     * Creates a default SentenceSplitter with
     * {@link LuceneConfig#MAIN_LANGUAGE} language.
     *
     * @throws IOException
     */
    public SentenceSplitter() throws IOException {
        this(LuceneConfig.MAIN_LANGUAGE);
    }

    public String[] splitSentences(final String text) {
        return sentenceDetector.sentDetect(text);
    }
}
