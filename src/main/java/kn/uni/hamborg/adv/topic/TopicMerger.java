/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.topic;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.AnalysisConfiguration;
import kn.uni.hamborg.adv.summary.Summaries;
import kn.uni.hamborg.adv.summary.Summary;
import kn.uni.hamborg.adv.table.FilterCell;
import kn.uni.hamborg.adv.table.TableManager;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import kn.uni.hamborg.utils.DocumentUtils;
import kn.uni.hamborg.utils.MapUtils;
import kn.uni.hamborg.web.cell.SummaryField;
//import mdsj.MDSJ;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;

/**
 * Merges topics which more similar to each other than a given similarity
 * threshold.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TopicMerger implements ITopicMerger {

    private static final Logger LOG = Logger.getLogger(TopicMerger.class.getSimpleName());

    private static final float mergeSimilarityThreshold = 0.3f;
    /**
     * Number of documents which are used for tfidf space computation
     */
    private static final int NUMBER_OF_DOCS_FOR_TFIDF = 10;
    /**
     * Size of merge radius for tfidf based DBScan
     */
    private static final double DBSCAN_MERGE_RADIUS = 0.01;

    private final TableManager tableManager;
    private final IndexReader indexReader;
    private final IndexSearcher indexSearcher;
    private final TopicExtractor topicExtractor;
    private final String fieldname;
    private final Analyzer analyzer;

    public TopicMerger(TableManager tableManager, IndexReader indexReader,
            IndexSearcher indexSearcher, TopicExtractor topicExtractor,
            Analyzer analyzer) {
        this.tableManager = tableManager;
        this.indexReader = indexReader;
        this.indexSearcher = indexSearcher;
        this.topicExtractor = topicExtractor;
        this.fieldname = AnalysisConfiguration.topicMergingSummarizationField;
        this.analyzer = analyzer;
    }

    private List<String> getTopDocIdsOfTopic(Topic topic) {
        final Document[] docs = TopicUtils.getDocumentsForTopic(topicExtractor, topic, tableManager, TopicSummarizer.NUMBER_OF_SUMMARIZATION_DOCS, indexSearcher, indexReader);
        return Arrays.asList(DocumentUtils.getIdsFromDocs(docs));

    }

    private String getTopSentences(Summary s, int k) {
        if (s.getTopSentences()[0].getValue().equals(Summaries.NOT_DEFINED)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(k, s.getTopSentences().length); i++) {
            sb.append(s.getTopSentences()[i].getValue());
            sb.append(" ");
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }

    private float calculateSummaryDistance(Summary a, Summary b) {
        String sentenceA = getTopSentences(a, 1);
        String sentenceB = getTopSentences(b, 1);
        if (sentenceA == null || sentenceB == null) {
            return 1.0f;
        }

        String[] tokensA = SimpleTokenizer.INSTANCE.tokenize(sentenceA);
        String[] tokensB = SimpleTokenizer.INSTANCE.tokenize(sentenceB);

        System.out.println(sentenceA);
        System.out.println(sentenceB);
        System.out.println("" + (1.0f - calculateTokenOverlap(Arrays.asList(tokensA), Arrays.asList(tokensB))));

        return 1.0f - calculateTokenOverlap(Arrays.asList(tokensA), Arrays.asList(tokensB));
    }

    private float calculateTermProbDifference(List<StringScore> a, List<StringScore> b) {
        return 0;
    }

    private float calculateTopTermDistance(Topic a, Topic b) {
        return 1.0f - calculateTokenOverlap(Arrays.asList(a.getTerms()), Arrays.asList(b.getTerms()));

    }

    private float calculateTokenOverlap(List<String> topDocsA, List<String> topDocsB) {
        float countIntersectedDocs = CollectionUtils.intersection(topDocsA, topDocsB).size();
        float maximumCountIntersectedDocs = Math.min(topDocsA.size(), topDocsB.size());
        System.out.println(topDocsA);
        System.out.println(topDocsB);
        return countIntersectedDocs / maximumCountIntersectedDocs;
    }

    static class TopicDoublePoint extends DoublePoint {

        private final Topic topic;

        public TopicDoublePoint(double[] point, Topic topic) {
            super(point);
            this.topic = topic;
        }

        public Topic getTopic() {
            return topic;
        }
    }

    private static double getTermProbability(List<StringScore> termProbs, String term) {
        for (StringScore ss : termProbs) {
            if (ss.getValue().equals(term)) {
                return ss.getScore();
            }
        }
        return 0.0;
    }

    /**
     * Uses the topic summarization sentence to compare topics!!! this should
     * really work because the sentences sometimes are almost the same
     *
     * @param topics
     * @return
     */
    private List<TopicDoublePoint> getTopicsInSummarySentenceTermSpace(List<Topic> topics) {
        // calculate the topic sentences
        final TopicSummarizer ts = new TopicSummarizer(tableManager, new SummaryField[]{SummaryField.fromLightDocField(fieldname)}, indexReader, indexSearcher, analyzer, topicExtractor);
        ts.computeSummaries();

        double[][] distanceMatrix = new double[topics.size()][topics.size()];

        for (int i = 0; i < distanceMatrix.length; i++) {
            final Topic topic = topics.get(i);
            final Summary topicSummary = TopicSummarizer.getSummary(topic, fieldname);

            for (int j = 0; j < distanceMatrix.length; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0.0;
                    continue;
                }

                final Topic mergeTopic = topics.get(j);
                final Summary mergeTopicSummary = TopicSummarizer.getSummary(mergeTopic, fieldname);

                //  distanceMatrix[i][j] = calculateSummaryDistance(topicSummary, mergeTopicSummary);
                distanceMatrix[i][j]
                        = calculateTopTermDistance(topic, mergeTopic);
                System.out.println("" + topic.getId() + " " + mergeTopic.getId() + ": " + distanceMatrix[i][j]);
            }
        }

        double[][] coords = null;//MDSJ.classicalScaling(distanceMatrix, 1);

        List<TopicDoublePoint> topicCoords = new ArrayList<>();

        for (int i = 0; i < topics.size(); i++) {
            double coorda = coords[0][i];
         //   double coordb = coords[1][i];
            //   double coordc = coords[2][i];
            //   double coordd = coords[3][i];
            //   double coorde = coords[4][i];
            System.out.println("" + topics.get(i).getId() + ": " + coorda);

            topicCoords.add(new TopicDoublePoint(new double[]{coorda}, topics.get(i)));
        }

        return topicCoords;
    }

    private List<TopicDoublePoint> getTopicsInWeightedTermSpace(List<Topic> topics) {
        List<TopicDoublePoint> topicsCoordinates = new ArrayList<>();

        TreeSet<String> tmp = new TreeSet<>();
        for (Topic topic : topics) {
            tmp.addAll(Arrays.asList(topic.getTerms()));
        }
        List<String> allTerms = new ArrayList<>(tmp);

        for (Topic topic : topics) {
            double[] topicCoords = new double[allTerms.size()];
            for (int i = 0; i < topicCoords.length; i++) {
                topicCoords[i] = getTermProbability(topic.getTermProbabilities(), allTerms.get(i));
            }
            topicsCoordinates.add(new TopicDoublePoint(topicCoords, topic));
        }

        return topicsCoordinates;
    }

    private List<TopicDoublePoint> getTopicsInDocumentTfidfSpace(List<Topic> topics) {
        try {
            List<TopicDoublePoint> topicsCoordinates = new ArrayList<>();
            TreeSet<String> tmpAllTerms = new TreeSet<>();
            Map<Topic, Map<String, Double>> topicAndTermScores = new HashMap<>();

            for (Topic topic : topics) {
                Map<String, Double> termScores = new HashMap<>();
                topicAndTermScores.put(topic, termScores);
                // currently only top doc is allowed
                ScoreDoc[] scoreDocs = TopicUtils.getScoreDocsForTopic(topicExtractor, topic, tableManager, NUMBER_OF_DOCS_FOR_TFIDF, indexSearcher, indexReader);
                for (ScoreDoc doc : scoreDocs) {
                    int id = doc.doc;

                    Terms docTerms = indexReader.getTermVector(id, fieldname);
                    if (docTerms == null) {
                        // ignore empty fields
                        continue;
                    }
                    TermsEnum docTermsIter = docTerms.iterator(TermsEnum.EMPTY);
                    BytesRef docTermRef = null;
                    while ((docTermRef = docTermsIter.next()) != null) {
                        String docTerm = docTermRef.utf8ToString();

                        // see if this is the same
                        //System.out.println(docTermsIter.docFreq());
                        //System.out.println(indexReader.docFreq(new Term(fieldname, docTermRef)));
                        double idf = indexReader.numDocs() / indexReader.docFreq(new Term(fieldname, docTermRef));
                        // even though the function is named totalTermFreq, it returns only the term freq in the current document
                        double tf = docTermsIter.totalTermFreq();
                        double tfidf = tf * Math.log(idf);

                        // save this term score for this topic
                        double termScore = termScores.getOrDefault(docTerm, 0.0);
                        termScores.put(docTerm, termScore + tfidf / NUMBER_OF_DOCS_FOR_TFIDF);

                        // add to all terms
                        tmpAllTerms.add(docTerm);
                    }
                }
            }

            // only use topic top terms for space
            // TEST
            tmpAllTerms = new TreeSet<>();
            for (Topic topic : topics) {
                tmpAllTerms.addAll(Arrays.asList(topic.getTopTerms()));
            }

            // fix all term order with list
            List<String> allTerms = new ArrayList<>(tmpAllTerms);
            System.out.println("space has " + allTerms.size() + " dimensions");

            // iterate all topics again and build coordinates (now that we have all terms)
            for (Topic topic : topics) {
                Map<String, Double> termScores = topicAndTermScores.get(topic);
                double[] coords = new double[allTerms.size()];
                for (int i = 0; i < coords.length; i++) {
                    String term = allTerms.get(i);
                    coords[i] = termScores.getOrDefault(term, 0.0);
                }
                topicsCoordinates.add(new TopicDoublePoint(coords, topic));
            }

            return topicsCoordinates;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private List<TopicDoublePoint> getTopicsInDocumentSpace(List<Topic> topics) {
        List<TopicDoublePoint> topicsCoordinates = new ArrayList<>();

        TreeSet<String> tmp = new TreeSet<>();
        Map<Topic, List<String>> topicAndTopDocs = new HashMap<>();
        for (Topic topic : topics) {
            List<String> docs = getTopDocIdsOfTopic(topic);
            topicAndTopDocs.put(topic, docs);
            tmp.addAll(docs);
        }

        // here we have all documents which occur in any topic
        final List<String> allDocs = new ArrayList<>(tmp);

        for (Topic topic : topics) {
            List<String> docs = topicAndTopDocs.get(topic);
            // this will hold the coordinates of this topic in document space
            double[] topicCoords = new double[allDocs.size()];
            for (int i = 0; i < topicCoords.length; i++) {
                // if the current doc id is in this document, we set the coordinate to 1, otherwise to 0
                topicCoords[i] = docs.contains(allDocs.get(i)) ? 1 : 0;
            }
            topicsCoordinates.add(new TopicDoublePoint(topicCoords, topic));
        }

        return topicsCoordinates;
    }

    @Override
    public void mergeTopics() {
        List<Topic> topics = topicExtractor.topics;
        DBSCANClusterer dbScan = new DBSCANClusterer(DBSCAN_MERGE_RADIUS, 0);//new DBSCANClusterer(0.03, 0);
        List<TopicDoublePoint> topicsInDocumentSpace = getTopicsInSummarySentenceTermSpace(topics);//getTopicsInDocumentTfidfSpace(topics);//getTopicsInWeightedTermSpace(topics);//getTopicsInDocumentSpace(topics);
        List<Cluster<TopicDoublePoint>> cluster = dbScan.cluster(topicsInDocumentSpace);

        // DBScanClusterer skips all points which are considered as noise
        // so we store all Topics which are clustered and just add the remaining, so that no
        // topic is missing
        List<Topic> usedTopics = new ArrayList<>();
        List<Topic> resultingTopics = new ArrayList<>();
        Map<Topic, Topic> oldTopicClusteredTopicAssignment = new HashMap<>();

        for (Cluster<TopicDoublePoint> c : cluster) {
            List<Topic> tmpTopics = new ArrayList<>();
            for (TopicDoublePoint topicPoint : c.getPoints()) {
                tmpTopics.add(topicPoint.getTopic());
                usedTopics.add(topicPoint.getTopic());
                System.out.println(topicPoint.getTopic().getId() + " " + Arrays.asList(topicPoint.getTopic().getTopTerms()));
            }
            Topic mergedTopic = Topic.mergeTopics(tmpTopics);
            resultingTopics.add(mergedTopic);

            for (TopicDoublePoint topicPoint : c.getPoints()) {
                oldTopicClusteredTopicAssignment.put(topicPoint.getTopic(), mergedTopic);
            }

            System.out.println("===============================");
        }

        List<Topic> remainingTopics = new ArrayList<>(topics);
        remainingTopics.removeAll(usedTopics);
        resultingTopics.addAll(remainingTopics);

        if (true) {
            return;
        }

        // update topic probabilities in all cells
        for (FilterCell cell : tableManager.getCells()) {
            Map<Topic, Double> newTopicProbs = new HashMap<>();
            if (cell.getTopicProbabilities() == null) {
                // some cells might have no docs and thus also no topics
                continue;
            }
            for (TopicScore ts : cell.getTopicProbabilities()) {
                Topic t = ts.getTopic();
                double p = ts.getScore();
                Topic clusteredTopic = oldTopicClusteredTopicAssignment.get(t);
                if (clusteredTopic != null) {
                    // this topic t has been clustered in a new clusteredTopic, thus we need to replace this in the cell
                    // we might already have this clustered topic in the new topic probs, in that case we add the probabilities
                    Double clusteredTopicProb = newTopicProbs.getOrDefault(clusteredTopic, 0.0);
                    // use the probability of the original topic
                    clusteredTopicProb += p;
                    newTopicProbs.put(clusteredTopic, clusteredTopicProb);

                    // also add this cell to the topic
                    if (!clusteredTopic.getCells().contains(cell)) {
                        clusteredTopic.addCell(cell);
                    }
                } else {
                    // this topic has not been clustered, so just add it to the new topic probs
                    newTopicProbs.put(t, p);
                }
            }

            cell.setTopics(MapUtils.mapToTopicScoreList(newTopicProbs));
        }

        LOG.log(Level.INFO, "merged topics: #{0} to #{1}", new Object[]{topics.size(), resultingTopics.size()});

        topicExtractor.topics = ImmutableList.copyOf(resultingTopics);

    }

}
