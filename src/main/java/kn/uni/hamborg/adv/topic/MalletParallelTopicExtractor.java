/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.topic;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Instance;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.table.FilterCell;
import kn.uni.hamborg.adv.table.FilterValue;
import kn.uni.hamborg.adv.table.TableManager;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import kn.uni.hamborg.utils.CSVUtils;
import kn.uni.hamborg.utils.DocumentUtils;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class MalletParallelTopicExtractor extends MalletTopicExtractor {

    private static final Logger LOG = Logger.getLogger(MalletParallelTopicExtractor.class.getSimpleName());
    private static final int NUM_TRAINING_CYCLES = 1500;

    private final ParallelTopicModel model;

    
    /**
     * Default: as many topics as we have cells.
     *
     * @param tableManager
     * @param fieldname
     * @param queryParser
     * @param indexReader
     * @param indexSearcher
     * @param analyzer
     */
    public MalletParallelTopicExtractor(TableManager tableManager, String fieldname, QueryParser queryParser, IndexReader indexReader, IndexSearcher indexSearcher,
            Analyzer analyzer) {
        this(tableManager, fieldname, queryParser, NUM_TOPICS_PER_CELL_, null, indexReader, indexSearcher, analyzer);
    }

    /**
     *
     * @param tableManager
     * @param fieldname
     * @param queryParser
     * @param numTopicsPerCell This defines only the desired ratio, i.e., how
     * many topics we should have in total, i.e., numTotalTopics =
     * numTopicsPerCell * cellCount. But it is not guaranteed that we will have
     * exactly this number of topics in a cell.
     * @param cellDocumentMergeType
     * @param indexReader
     * @param indexSearcher
     * @param analyzer
     */
    public MalletParallelTopicExtractor(TableManager tableManager,
            String fieldname, QueryParser queryParser, int numTopicsPerCell,
            CellDocumentMergeType cellDocumentMergeType, IndexReader indexReader, IndexSearcher indexSearcher, Analyzer analyzer) {
        super(tableManager, fieldname, queryParser, numTopicsPerCell, cellDocumentMergeType, indexReader, indexSearcher, analyzer);

        // http://stats.stackexchange.com/questions/37405/natural-interpretation-for-lda-hyperparameters
        model = new ParallelTopicModel(numTopics, numTopics * 0.00001, 0.0001);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(LuceneConfig.INDEX_NUMBER_OF_THREADS);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(NUM_TRAINING_CYCLES);
    }

    /**
     * Adds all cell documents to the mallet pipeline. Thereby all documents
     * from one cell are concatenated and added as a single instance (belonging
     * to that cell virtually) to mallet.
     */
    private void addAllCellDocumentsAsConcatenatedInstances() {
        int instancePosition = 0;
        for (FilterValue rowDimension : tableManager.getRowDimension()) {
            for (FilterValue colDimension : tableManager.getColDimension()) {
                // get all docs of this cell, get their text, concat it and put it as one instance into mallet
                final FilterCell cell = tableManager.getCell(rowDimension, colDimension);
                final Document[] cellDocs = cell.getDocuments().toArray(new Document[0]);
                final String cellDocsText = DocumentUtils.getConcatenatedContent(cellDocs, fieldname);

                LOG.log(Level.INFO, "creating topic instance for cell {0}", cell.getHumanReadableId());

                instances.addThruPipe(new Instance(cellDocsText,
                        cell.getId(), cell.getId(), cell.getId()));

                List<Integer> cellPositions = cellInstancePositions.get(cell);
                if (cellPositions == null) {
                    cellPositions = new ArrayList<>();
                    cellInstancePositions.put(cell, cellPositions);
                }
                cellPositions.add(instancePosition);
                instancePosition++;
            }
        }
    }

    /**
     * Adds all cell documents to mallets pipeline. Thereby each document is
     * added as a single instance. Thus, there is no 1:1 relation between cell
     * and instance, but rather a 1:N relation between cell and instances.
     */
    private void addAllCellDocumentsAsSingleInstances() {
        final boolean exportit = false;
        org.dom4j.Document doc = null;
        Element topic = null;
        if (exportit) {
            doc = DocumentHelper.createDocument();
            Element section = doc.addElement("section");
            section.addAttribute("name", "noname");
            topic = section.addElement("topic");
            topic.addAttribute("name", "noname");
        }
        int instancePosition = 0;
        for (FilterValue rowDimension : tableManager.getRowDimension()) {
            for (FilterValue colDimension : tableManager.getColDimension()) {
                // get all docs of this cell, get their text, concat it and put it as one instance into mallet
                final FilterCell cell = tableManager.getCell(rowDimension, colDimension);
                final Document[] cellDocs = cell.getDocuments().toArray(new Document[0]);

                LOG.log(Level.INFO, "creating topic instance for cell {0}", cell.getHumanReadableId());

                for (Document cellDoc : cellDocs) {
                    final String cellDocText = cellDoc.get(fieldname);
                    instances.addThruPipe(new Instance(cellDocText,
                            cell.getId(), cell.getId() + instancePosition, cell.getId()));
                    if (exportit) {
                        Element utterance = topic.addElement("utterance");
                        utterance.addAttribute("name", cell.getHumanReadableId());
                        utterance.addAttribute("sprecherId", "tbd");
                        utterance.addAttribute("timestamp", "tbd");
                        utterance.addAttribute("id", "0");
                        utterance.addText(cellDocText.replaceAll("\\P{InBasic_Latin}", ""));
                    }
                    List<Integer> cellPositions = cellInstancePositions.get(cell);
                    if (cellPositions == null) {
                        cellPositions = new ArrayList<>();
                        cellInstancePositions.put(cell, cellPositions);
                    }
                    cellPositions.add(instancePosition);
                    instancePosition++;
                }
            }
        }

        if (exportit) {
            try {
                // Pretty print the document to System.out
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter writer;
                writer = new XMLWriter(new FileWriter("C:\\testtest.xml"), format);
                writer.write(doc);
                writer.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    @Override
    public void computeTopics() {
        switch (cellDocumentMergeType) {
            case ALL_DOCS_ONE_INSTANCE:
                addAllCellDocumentsAsConcatenatedInstances();
                break;
            case ONE_DOC_ONE_INSTANCE:
                addAllCellDocumentsAsSingleInstances();
                break;
            default:
                throw new IllegalArgumentException();
        }

        // add all instance to the model
        model.addInstances(instances);

        final DateTime starttime = DateTime.now();
        LOG.log(Level.INFO, "starting calculation of {0} topics on {1} data instances", new Object[]{numTopics, instances.size()});
        try {
            model.estimate();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        final int secs = Seconds.secondsBetween(starttime, DateTime.now()).getSeconds();
        LOG.log(Level.INFO, "calculation of topics finished after {0} seconds ({1} s/topic)", new Object[]{secs, (float) secs / numTopics});

        // create topic objects.
        final List<Topic> allTopics = new ArrayList<>(numTopics);
        for (int topic = 0; topic < numTopics; topic++) {
            final List<StringScore> termWeights = new ArrayList<>();

            for (int type = 0; type < model.numTypes; type++) {
                int[] topicCounts = model.typeTopicCounts[type];
                double weight = model.beta;
                int index = 0;
                while (index < topicCounts.length
                        && topicCounts[index] > 0) {
                    int currentTopic = topicCounts[index] & model.topicMask;
                    if (currentTopic == topic) {
                        weight += topicCounts[index] >> model.topicBits;
                        break;
                    }
                    index++;
                }

                termWeights.add(new StringScore(model.getAlphabet().lookupObject(type).toString(), weight));
            }

            // add topic (which internally converts weights into probabilities and removes unprobable terms)
            allTopics.add(new Topic(termWeights, topic));
        }

        if (Topic.TOPIC_TERM_STATS_ENABLED) {
        // Statistics: Here we have all the weight distribution of all terms in all topics present in Topic.freqs
            // accumulate all of that
            TreeMap<Double, Integer> tm = new TreeMap<>(Topic.freqs);
            TreeMap<Double, Double> tm2 = new TreeMap<>();

            // iterate it in descending order
            double previousSum = 0.0;
            for (Double key : tm.descendingKeySet()) {
                Integer count = tm.get(key);
                Double cur = (double) count / numTopics;
                previousSum += cur;
                tm2.put(key, previousSum);
            }
            CSVUtils.writeToCSV(new File("/home/felix/stats.csv"), tm2);
        }

        // now also add cells to topics
        Set<Topic> usedTopics = new HashSet<>();
        
        // iterate all cells
        for (Map.Entry<FilterCell, List<Integer>> entrySet : cellInstancePositions.entrySet()) {
            FilterCell cell = entrySet.getKey();
            // this contains all instance / documents indexes for the current cell
            List<Integer> instancePos = entrySet.getValue();

            RealVector cellTopicProbabilities = null;

            // because we have multiple cell documents, we need to aggregate their particular topic probabilities
            // current approach: just sum all probs up and divide by number of cell docs to get the average
            // maybe another idea is to use the max values or use the quartile or something
            for (Integer singleInstancePos : instancePos) {
                double[] topicProbsOfCell = model.getTopicProbabilities(singleInstancePos);
                RealVector cellDocTopicProbabilities = MatrixUtils.createRealVector(topicProbsOfCell);
                
                // sum up
                if (cellTopicProbabilities == null) {
                    cellTopicProbabilities = cellDocTopicProbabilities;
                } else {
                    cellTopicProbabilities = cellTopicProbabilities.add(cellDocTopicProbabilities);
                }
            }
            // compute the average by performing division
            cellTopicProbabilities = cellTopicProbabilities.mapDivide(instancePos.size());
            
            
            Map<Topic, Double> tmpCellTopicProbabilities = new HashMap<>();
            for (int i = 0; i < allTopics.size(); i++) {
                tmpCellTopicProbabilities.put(getTopicById(allTopics, i), cellTopicProbabilities.toArray()[i]);
            }
            usedTopics.addAll(createRelevantTopicCellMappings(allTopics, tmpCellTopicProbabilities, cell));
        }

        for (Map.Entry<FilterCell, List<Integer>> entrySet : cellInstancePositions.entrySet()) {
            FilterCell cell = entrySet.getKey();
            LOG.log(Level.INFO, "{0}: {1}", new Object[]{cell.getHumanReadableId(), cell.getTopicProbabilities().size()});
            LOG.info(cell.getTopicProbabilities().toString());
        }

        // TODO: now check which topics are used nowhere and remove them
        //allTopics.removeAll(usedTopics);
        if (usedTopics.size() < allTopics.size()) {
            LOG.log(Level.WARNING, "not all topics have been used: {0} / {1}", new Object[]{usedTopics.size(), allTopics.size()});
        }

        topics = ImmutableList.copyOf(usedTopics);

        LOG.info("finished creation of topics");
    }

}
