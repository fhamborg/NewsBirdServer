/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.io.IOException;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.language.Language;
import org.apache.lucene.document.Document;

/**
 * Provides sentiment analysis. Currently uses CoreNLP. However, the quality of
 * the results is not the best, so this might be a TODO to change it to maybe
 * use NLTK (via REST).
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class SentimentAnalyzer {

    private static final Logger LOG = Logger.getLogger(SentimentAnalyzer.class.getSimpleName());

    private final StanfordCoreNLP pipeline;

    protected final Language language;
    private final SentenceSplitter sentenceSplitter;

    /**
     * Not implemented yet, use the constructor without params.
     *
     * @param language
     * @throws java.io.IOException
     */
    public SentimentAnalyzer(final Language language) throws IOException {
        if (language != Language.EN) {
            throw new RuntimeException("lang=" + language + " not implemented yet");
        }
        this.language = language;
        this.sentenceSplitter = new SentenceSplitter(language);

        final Properties props = new Properties();

        // taken from: https://blog.openshift.com/day-20-stanford-corenlp-performing-sentiment-analysis-of-twitter-using-java/
        // also, by looking at this https://github.com/stanfordnlp/CoreNLP/blob/master/src/edu/stanford/nlp/sentiment/SentimentPipeline.java 
        // it seems reasonable to have "only" these annotators being activated
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");

        pipeline = new StanfordCoreNLP(props);

        LOG.log(Level.INFO, "created {0} instance", SentimentAnalyzer.class.getSimpleName());
    }

    public static void main(String[] args) throws IOException {
        SentimentAnalyzer sa = new SentimentAnalyzer(Language.EN);
        sa.calcSentiment("Best."); // 3
        sa.calcSentiment("Good."); // 3
        sa.calcSentiment("Okay."); // 2
        sa.calcSentiment("Normal."); // 2
        sa.calcSentiment("Bad."); // 1
        sa.calcSentiment("Worst."); // 2
        sa.calcSentiment("Sad."); // 2
        sa.calcSentiment("I am a sad guy."); // 2
        sa.calcSentiment("This is war."); // 1
        sa.calcSentiment("I will punch you right in your stupid face."); // 3
        sa.calcSentiment("Death"); // 1
        sa.calcSentiment("He will kill her And afterwards she is going to die."); // 1
        sa.calcSentiment("He will kill her And afterwards she is going to die. LOL that is funny."); // 2
        sa.calcSentiment("He will kill her And afterwards she is going to die. "
                + "LOL that is funny and hilarious. I am laughing! We are having a lot of fun. "
                + "Enjoy. Fun. Nice. Cool. Good."); // 3
    }

    /**
     * Calculates the sentiment of a given {@code doc} by extracting the text
     * from its field named {@code fieldname}. Then the text is split by
     * sentences and the sentiments are calculated and aggregated for each
     * sentence. The sum (if positive or negative) is used to determine the
     * total sentiment.
     *
     * @param doc
     * @param fieldname
     * @return
     */
    public DocumentSentiment calcSentiment(Document doc, String fieldname) {
        final String text = doc.get(fieldname);
        final String[] sentences = sentenceSplitter.splitSentences(text);

        float sentimentSum = 0;
        for (String sentence : sentences) {
            Sentiment s = calcSentiment(sentence);
            sentimentSum += s.getAsNumber();
        }
        sentimentSum /= sentences.length;

        return new DocumentSentiment(doc, sentimentSum);
    }

    /**
     * Calculates the sentiment of {@code sentence}. It is necessary that
     * {@code sentence} is really only one single sentence! Otherwise only the
     * longest sentence is considered.
     *
     * @param sentence
     * @return
     */
    public Sentiment calcSentiment(String sentence) {
        int mainSentiment = 0;
        if (sentence != null && sentence.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(sentence);
            for (CoreMap sentenceTree : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentenceTree.get(SentimentCoreAnnotations.AnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentenceTree.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }
            }
        }
        //System.out.println(sentence + " " + mainSentiment);

        switch (mainSentiment) {
            case 1:
                return Sentiment.NEGATIVE;
            case 3:
                return Sentiment.GOOD;
            default:
                return Sentiment.NEUTRAL;
        }

        /* Old code which performs not so well, because it works only word based
         .
         Annotation annotation = pipeline.process(sentence);
         int posCount = 0, negCount = 0;

         List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
         for (CoreMap sent : sentences) {
         String sentiment = sent
         .get(SentimentCoreAnnotations.ClassName.class);
         switch (sentiment) {
         case "Positive":
         posCount++;
         break;
         case "Negative":
         negCount++;
         break;
         }
         // System.out.println(sentiment + "\t" + sent);
         }

         if (posCount > negCount) {
         //  System.out.println("good");
         return Sentiment.GOOD;
         } else if (negCount > posCount) {
         //  System.out.println("neg");
         return Sentiment.NEGATIVE;
         } else {
         //  System.out.println("neut");
         return Sentiment.NEUTRAL;
         }
         */
    }
}
