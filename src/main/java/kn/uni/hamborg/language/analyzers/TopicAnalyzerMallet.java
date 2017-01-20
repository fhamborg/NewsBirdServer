/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.topics.NPTopicModel;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.utils.LightDocUtils;
import kn.uni.hamborg.utils.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TopicAnalyzerMallet {

    private static final Logger LOG = Logger.getLogger(TopicAnalyzerMallet.class.getSimpleName());

    private final InstanceList instances;
    private ParallelTopicModel model;
    private final int numTrainingCycles = 1500;
    private String[][] topWordsByTopic;

    /**
     * If the probability for a topic is less than this, we don't add it
     */
    final float probThreshold = 0.2f;

    public TopicAnalyzerMallet() {
        // Begin by importing documents from text to feature sequences
        List<Pipe> pipeList = new ArrayList<>();

        // TODO: disable logger
        // TODO: reuse this during indexing so that we already have what we need in the indexes of lucene
        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        TokenSequenceRemoveStopwords stopwords = new TokenSequenceRemoveStopwords();
        Iterator iter = EnglishAnalyzer.getDefaultStopSet().iterator();
        String[] tmpstopword = new String[EnglishAnalyzer.getDefaultStopSet().size()];
        int i = 0;
        while (iter.hasNext()) {
            char[] word = (char[]) iter.next();
            tmpstopword[i++] = new String(word);
        }
        stopwords.addStopWords(tmpstopword);
        pipeList.add(stopwords);
        pipeList.add(new TokenSequence2FeatureSequence());

        instances = new InstanceList(new SerialPipes(pipeList));
    }

    public TopicAnalyzerMallet addContents(String[] contents) {
        for (int i = 0; i < contents.length; i++) {
            String content = contents[i];
            instances.addThruPipe(new Instance(content, "X", i, i));
        }
        return this;
    }

    /**
     * Performs Mallet's topic analysis.
     *
     * @param numTopics If {@code < 0} numTopics is set to number of
     * {@code instances}.
     * @return
     */
    public TopicAnalyzerMallet runTopicAnalysis(int numTopics) {
        if (numTopics < 0) {
            numTopics = instances.size();
        }

        /**
         * Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01 Note
         * that the first parameter is passed as the sum over topics, while the
         * second is the parameter for a single dimension of the Dirichlet
         * prior.
         *
         * BETA (from wikipedia_en): prior weight of word w in a topic; usually
         * the same for all words; normally a number much less than 1, e.g.
         * 0.001, to strongly prefer sparse word distributions, i.e. few words
         * per topic
         */
        model = new ParallelTopicModel(numTopics, numTopics * 0.01, 0.001);
        model.setNumThreads(LuceneConfig.INDEX_NUMBER_OF_THREADS);
        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(LuceneConfig.INDEX_NUMBER_OF_THREADS);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(numTrainingCycles);
        try {
            model.estimate();

            Object[][] tmpTopWords = model.getTopWords(10);
            topWordsByTopic = new String[tmpTopWords.length][];
            for (int i = 0; i < tmpTopWords.length; i++) {
                Object[] tmpTopWord = tmpTopWords[i];
                topWordsByTopic[i] = new String[tmpTopWord.length];
                for (int j = 0; j < tmpTopWord.length; j++) {
                    topWordsByTopic[i][j] = tmpTopWord[j].toString();
                }
            }

            LOG.info("finished model computation");
            return this;
        } catch (IOException ex) {
            Logger.getLogger(TopicAnalyzerMallet.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public String getTopTermsForTopicIndex(int topicIndex, boolean luceneQueryMode) {
        StringBuilder sb = new StringBuilder();
        int rank = 10;
        for (int i = 0; i < topWordsByTopic[topicIndex].length; i++) {
            if (rank-- == 0) {
                break;
            }
            String topWord = topWordsByTopic[topicIndex][i];
            if (luceneQueryMode) {
                sb.append("\"" + topWord + "\" ");
            } else {
                sb.append(topWord);
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public double getTopicProbability(int docIndex, int topicIndex) {
        return model.getTopicProbabilities(docIndex)[topicIndex];
    }

    public int[] getMostImportantTopicIndexesForContentReadable(int docIndex) {
        double[] topicProbs = model.getTopicProbabilities(docIndex);
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < topicProbs.length; i++) {
            double topicProb = topicProbs[i];
            if (topicProb >= probThreshold) {
                indexes.add(i);
            }
        }

        return ArrayUtils.toPrimitive(indexes.toArray(new Integer[0]));
    }

    public String[] getMostImportantTopicPerContentReadable() {
        String[] infos = new String[instances.size()];
        for (int i = 0; i < instances.size(); i++) {
            double[] topicProbs = model.getTopicProbabilities(i);
            final Map<Integer, Double> mapTopicProbs = new HashMap<>();
            for (int j = 0; j < topicProbs.length; j++) {
                double topicProb = topicProbs[j];
                mapTopicProbs.put(j, topicProb);
            }
            final SortedSet<Map.Entry<Integer, Double>> sortedEntries = MapUtils.entriesSortedByValues(mapTopicProbs, true);
            int rank = 10;

            final Formatter out = new Formatter();

            for (Map.Entry<Integer, Double> entrySet : sortedEntries) {
                if (rank-- == 0) {
                    break;
                }
                Integer topicId = entrySet.getKey();
                Double topicProb = entrySet.getValue();
                if (topicProb < probThreshold) {
                    continue;
                }
                out.format("%d (%f) [" + getTopTermsForTopicIndex(topicId, false) + "]; ", topicId, topicProb
                );
            }
            out.format(System.lineSeparator());
            // System.out.println(out.toString());
            infos[i] = out.toString();
        }
        return infos;
    }

    /**
     * Show top 5 words in topics with proportions for the first document
     *
     * @return
     */
    public TopicAnalyzerMallet printMostImportantTermsPerTopic() {
        Object[][] topWordsPerTopic = model.getTopWords(10);

        for (int i = 0; i < topWordsPerTopic.length; i++) {
            Object[] topWords = topWordsPerTopic[i];
            System.out.println("" + i + ": " + Arrays.toString(topWords));
        }

        return this;
    }

    // http://mallet.cs.umass.edu/topics-devel.php
    public static void test(IndexReader indexReader, Document[] docs) throws IOException {
        TopicAnalyzerMallet m = new TopicAnalyzerMallet();

        for (int j = 0; j < docs.length; j++) {
            Document doc = docs[j];
            final String id = LightDocUtils.getId(doc);
            final String label = "X";
            final String content = LightDocUtils.getContent(doc);
            m.instances.addThruPipe(new Instance(content, label, id, id));
        }

        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        int numTopics = 100;
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

        model.addInstances(m.instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(LuceneConfig.INDEX_NUMBER_OF_THREADS);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(1000);
        model.estimate();

        LOG.info("finished model computation");

        // Show the words and topics in the first instance
        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = m.instances.getDataAlphabet();

        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        LabelSequence topics = model.getData().get(0).topicSequence;

        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        for (int position = 0; position < tokens.getLength(); position++) {
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
        }
        LOG.info("topic distribution of first article (token - topic number)");
        LOG.info(out.toString());

        // Estimate the topic distribution of the first instance, 
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

        // Show top 5 words in topics with proportions for the first document
        System.out.println("Showing the top 5 words for each topic (1 topic = 1 row) with frequencies");
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            String luceneQuery = "";
            while (iterator.hasNext() && rank < 5) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                luceneQuery += "+" + dataAlphabet.lookupObject(idCountPair.getID()) + " ";
                rank++;
            }
            System.out.println(out);
        }

        /**
         * create an article with top 5 words from topic 0 and check the
         * predicted probability of that that it is topic 0
         */
        // Create a new instance with high probability of topic 0
        StringBuilder topicZeroText = new StringBuilder();
        Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

        int rank = 0;
        while (iterator.hasNext() && rank < 5) {
            IDSorter idCountPair = iterator.next();
            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
            rank++;
        }

        // Create a new instance named "test instance" with empty target and source fields.
        InstanceList testing = new InstanceList(m.instances.getPipe());
        testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));

        TopicInferencer inferencer = model.getInferencer();
        double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
        System.out.println("probability of being topic 0\t" + testProbabilities[0]);
    }
}
