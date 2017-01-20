/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.lucene.summarizer;

import kn.uni.hamborg.utils.MapUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.AnalysisConfiguration;
import kn.uni.hamborg.language.analyzers.SentenceSplitter;
import kn.uni.hamborg.utils.IndexUtils;
import kn.uni.hamborg.utils.DocumentUtils;
import kn.uni.hamborg.utils.LightDocUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Bits;

/**
 * Provides basic functionality to summarize documents.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public abstract class Summarizer {

    private static final Logger LOG = Logger.getLogger(Summarizer.class.getSimpleName());

    private static final int DEFAULT_NUMBER_OF_SENTENCES_THAT_ARE_SUMMARIZED = 10;

    static {
        initNLP();

    }
    private static SentenceSplitter sentenceSplitter;
    protected final IndexReader indexReader;
    protected final IndexSearcher indexSearcher;
    protected final Analyzer analyzer;

    private Map<String, Set<String>> sentenceLightDocIds;

    /**
     * Constructs a Summarizer instance by creating a temporary
     * {@link Directory} containing the given {@code documents}.
     *
     * @param documents
     * @param analyzer
     * @throws IOException
     */
    public Summarizer(Document[] documents, Analyzer analyzer) throws IOException {
        final Directory tmpDir = IndexUtils.createTemporaryDir(documents);
        this.indexReader = IndexUtils.createIndexReader(tmpDir);
        this.indexSearcher = IndexUtils.createIndexSearcher(indexReader);
        this.analyzer = analyzer;

    }

    /**
     * Constructs a Summarizer.
     *
     * @param indexReader
     * @param indexSearcher
     * @param analyzer
     */
    public Summarizer(IndexReader indexReader, IndexSearcher indexSearcher, Analyzer analyzer) {
        this.indexReader = indexReader;
        this.indexSearcher = indexSearcher;
        this.analyzer = analyzer;

    }

    /**
     * Constructs a Summarizer by creating virtual documents, one for each given
     * String in {@code contents}.
     *
     * @param contents
     * @param analyzer
     * @throws IOException
     */
    public Summarizer(String[] contents, Analyzer analyzer) throws IOException {
        final List<Document> docs = new ArrayList<>();
        for (String content : contents) {
            docs.add(DocumentUtils.createDocumentFromString(content));
        }
        final Directory tmpDir = IndexUtils.createTemporaryDir(docs.toArray(new Document[0]));
        this.indexReader = IndexUtils.createIndexReader(tmpDir);
        this.indexSearcher = IndexUtils.createIndexSearcher(indexReader);
        this.analyzer = analyzer;
    }

    /**
     * Initializes OpenNLP & CoreNLP components.
     */
    private static void initNLP() {
        try {
            sentenceSplitter = new SentenceSplitter();
            // Tokenizer tokenizer = new TokenizerME(new TokenizerModel(new File("opennlpmodels/" + lang + "-token.bin")));
            // POSTagger posTagger = new POSTaggerME(new POSModel(new File("opennlpmodels/" + lang + "-pos-maxent.bin")));
            LOG.info("initialized NLP components successfully");
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "initialization of OpenNLP components failed", ioe);
            throw new RuntimeException();
        }
    }

    /**
     * Returns the top k sentences. Therefore, {@link #calculateTermScore(org.apache.lucene.index.IndexReader, java.lang.String)
     * } is invoked.
     *
     * @param fieldname
     * @param indexReader
     * @param k
     * @return
     * @throws IOException
     */
    public StringScore[] getTopKSentences(
            final String fieldname,
            final int k) throws IOException {
        final Map<String, Double> termScore = calculateTermScore(fieldname);
        return getTopKSentences(fieldname, k, termScore);
    }

    /**
     * Returns the top k tokens. Therefore, {@link #calculateTermScore(org.apache.lucene.index.IndexReader, java.lang.String)
     * } is invoked.
     *
     * @param fieldname
     * @param k
     * @return
     * @throws IOException
     */
    public StringScore[] getTopKTokens(
            final String fieldname,
            final int k) throws IOException {
        final Map<String, Double> termScore = calculateTermScore(fieldname);
        //System.out.println(termScore);
        List<StringScore> topTokens = MapUtils.mapToStringScoreList(termScore);
        MapUtils.sortByObjectScores(topTokens, true);

        return topTokens.subList(0, Math.min(k, topTokens.size())).toArray(new StringScore[0]);
    }

    /**
     * Returns the top k sentences based on {@code termScore}.
     *
     * @param fieldname
     * @param termScore
     * @param k
     * @return
     * @throws IOException
     */
    protected StringScore[] getTopKSentences(
            final String fieldname,
            final int k,
            final Map<String, Double> termScore) throws IOException {
        //  final Analyzer analyzer = AnalyzerFactory.createAnalyzerForField(fieldname);

        Bits liveDocs = MultiFields.getLiveDocs(indexReader);
        // save the sentence and the score
        Map<String, Double> topSentenceScore = new HashMap<>();
        // save the sentence and the light doc ids
        sentenceLightDocIds = new HashMap<>();
        for (int i = 0; i < indexReader.maxDoc(); i++) {
            if (liveDocs != null && !liveDocs.get(i)) {
                continue;
            }
            Document doc = indexReader.document(i);
            String docText = doc.get(fieldname);
            Set<String> lightDocIds = sentenceLightDocIds.get(docText);
            if (lightDocIds == null) {
                lightDocIds = new HashSet<>();
                sentenceLightDocIds.put(docText, lightDocIds);
            }
            lightDocIds.add(LightDocUtils.getId(doc));

            topSentenceScore = MapUtils.mergeMapsAndSumValuesForEqualKeys(
                    topSentenceScore,
                    getTopSentencesFromDocumentText(termScore, docText, k));
        }

        SortedSet<Map.Entry<String, Double>> ss
                = MapUtils.entriesSortedByValues(topSentenceScore, true);
        List<StringScore> topKSentences = new ArrayList<>();

        int curCount = k;
        for (Map.Entry<String, Double> entrySet : ss) {
            if (curCount <= 0) {
                break;
            }
            String key = entrySet.getKey();
            Double score = entrySet.getValue();
            topKSentences.add(new StringScore(key, score));
            curCount--;
        }
        return topKSentences.toArray(new StringScore[0]);
    }

    /**
     * Splits {@code sentence} into its tokens (which are the same as we had
     * during indexing, if we use the same {@link Analyzer}). Each token is then
     * compared with its score from the {@code termTfidf} map. The sum of all
     * found scores is returned.
     *
     * @param analyzer
     * @param sentence
     * @param termTfidf
     * @return
     */
    public double scoreSentence(String sentence, Map<String, Double> termTfidf) {
        double score = 0;
        try {
            TokenStream ts = analyzer.tokenStream("egal", sentence);
            ts.reset();
            CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
            while (ts.incrementToken()) {
                String token = new String(termAtt.buffer(), 0, termAtt.length());
                if (token != null) {
                    Double tfidf = termTfidf.get(token);
                    if (tfidf != null) {
                        score += tfidf;
                    }
                }
            }
            ts.close();
        } catch (IOException ex) {
            Logger.getLogger(Summarizer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return score;
    }

    protected Map<String, Double> getTopSentencesFromDocumentText(
            Map<String, Double> termTfidf, String text, int k) {

        final Map<String, Double> sentenceScore = new HashMap<>();
        final String[] sentences = sentenceSplitter.splitSentences(text);

        /**
         * If this is true, the sentences will get an additional, artifical
         * score that is very high to ensure that the sentence is placed where
         * it would be placed in a document. For example, a sentence that is the
         * second in a document will get a higher score than another sentence
         * that is placed at the 5th position in the same or another document.
         *
         * If two sentence have the same position within a document, their
         * actual summarization score will decide.
         */
        final boolean orderSentencesByFirstOccurenceInDoc = AnalysisConfiguration.summarization_OrderSentencesByFirstOccurenceInDoc;
        final double positionScoreFactor = AnalysisConfiguration.summarization_OrderSentencesByFirstOccurenceInDoc_PositionScoreFactor;
        /**
         * According to Lin2002Single we keep only first k (=10) sentences or
         * all.
         */
        final int firstKSentences_k = AnalysisConfiguration.summarization_Lin2002Single_FirstSentencesOnly ? DEFAULT_NUMBER_OF_SENTENCES_THAT_ARE_SUMMARIZED : Integer.MAX_VALUE;

        for (int i = 0; i < Math.min(firstKSentences_k, sentences.length); i++) {
            final String sentence = sentences[i];

            double score = scoreSentence(sentence, termTfidf);
            if (orderSentencesByFirstOccurenceInDoc) {
                // the greater i (the position of the sentence within the document), the less the additional position score
                score += positionScoreFactor / (i + 1.0);
            }
            sentenceScore.put(sentence, score);
        }

        SortedSet<Map.Entry<String, Double>> ss
                = MapUtils.entriesSortedByValues(sentenceScore, true);
        Map<String, Double> topKSentences = new HashMap<>();

        for (Map.Entry<String, Double> entrySet : ss) {
            if (k <= 0) {
                break;
            }
            String key = entrySet.getKey();
            topKSentences.put(key, entrySet.getValue());
            k--;
        }

        return topKSentences;
    }

    public Map<String, Set<String>> getSentenceLightDocIds() {
        return sentenceLightDocIds;
    }

    /**
     * Calculates a score for each term. The greater the score, the better.
     *
     * @param reader
     * @param field
     * @return
     * @throws IOException
     */
    protected abstract Map<String, Double> calculateTermScore(final String field) throws IOException;
}
