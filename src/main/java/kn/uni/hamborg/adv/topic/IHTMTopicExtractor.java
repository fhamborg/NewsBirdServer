/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.topic;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.table.FilterCell;
import kn.uni.hamborg.adv.table.TableManager;
import kn.uni.hamborg.config.FileConfig;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class IHTMTopicExtractor extends TopicExtractor {

    private static final Logger LOG = Logger.getLogger(IHTMTopicExtractor.class.getSimpleName());

    private static final File basePath = Paths.get(FileConfig.basePathMpProjectData + "topics").toFile();

    public IHTMTopicExtractor(TableManager tableManager, String fieldname, QueryParser queryParser, IndexReader indexReader, IndexSearcher indexSearcher, Analyzer analyzer) {
        super(tableManager, fieldname, queryParser, indexReader, indexSearcher, analyzer);

        topicProbabilityThreshold = 0.1;
    }

    /**
     * Gets for each cell the topic probabilities.
     *
     * @param jsonDocuments
     */
    private Map<FilterCell, Map<Topic, Double>> getCellTopicProbabilities(List<Topic> topics, JsonArray jsonDocuments) {
        final JsonParser parser = new JsonParser();
        final Map<FilterCell, Map<Topic, Double>> cellTopicProbabilities = new HashMap<>();
        final Map<FilterCell, Integer> cellDocumentCounts = new HashMap<>();

        for (JsonElement tmp : jsonDocuments) {
            JsonObject jsonDoc = tmp.getAsJsonObject();
            String id = jsonDoc.get("id").getAsString();
            FilterCell cell = tableManager.getCellByHumanReadableId(jsonDoc.get("speaker").getAsString());
            cellDocumentCounts.put(cell, cellDocumentCounts.getOrDefault(cell, 0) + 1);

            Map<Topic, Double> topicProbs = cellTopicProbabilities.get(cell);
            if (topicProbs == null) {
                topicProbs = new HashMap<>();
                cellTopicProbabilities.put(cell, topicProbs);
            }

            try {
                JsonObject topicProbabilitiesOfCurCellDoc = parser.parse(jsonDoc.get("topicSimialrity").getAsString()).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entrySet : topicProbabilitiesOfCurCellDoc.entrySet()) {
                    int curTopicId = Integer.valueOf(entrySet.getKey());
                    double curTopicProb = entrySet.getValue().getAsDouble();
                    double tmpTopicProb = topicProbs.getOrDefault(curTopicId, 0.0);

                    Topic curTopic = getTopicById(topics, curTopicId);
                    if (curTopic == null) {
                        //LOG.severe("no topic for id " + curTopicId);
                    } else {
                        topicProbs.put(curTopic, tmpTopicProb + curTopicProb);
                    }
                }
            } catch (Exception e) {
                //System.out.println(jsonDoc.toString());
                //e.printStackTrace();
                LOG.warning(e.getMessage());
            }
        }

        // normalize
        for (Map.Entry<FilterCell, Map<Topic, Double>> entrySet : cellTopicProbabilities.entrySet()) {
            FilterCell key = entrySet.getKey();
            Integer docCount = cellDocumentCounts.get(key);
            Map<Topic, Double> value = entrySet.getValue();

            for (Map.Entry<Topic, Double> entrySet1 : value.entrySet()) {
                Topic key1 = entrySet1.getKey();
                Double value1 = entrySet1.getValue();
                value.put(key1, value1 / docCount);
            }
        }

        // at this point we have for each FilterCell its topic probabilities
        return cellTopicProbabilities;
    }

    /**
     *
     * @param jsonTopic
     * @return
     */
    private Topic processJsonTopic(int i, JsonObject jsonTopic) {
        final String topicId = "" + i;//jsonTopic.get("id").getAsString();
        final List<StringScore> terms = new ArrayList<>();

        //System.out.println(jsonTopic.get("name").getAsString());
        int count = 0;
        for (String s : jsonTopic.get("name").getAsString().split(" ")) {
            //System.out.println(s);
            terms.add(new StringScore(s, 1.0 - count * 0.0001));
            count++;
            // we do this count stuff here to preserve the order of the terms (right now there is no way of getting the actual term probability)
        }
        final Topic topic = new Topic(terms, Integer.valueOf(topicId));
        return topic;
    }

    @Override
    public void computeTopics() {
        try {
            File tpJson = new File(basePath, "tp.json");
            File utrJson = new File(basePath, "utr.json");
            JsonObject rootTp = new JsonParser().parse(new FileReader(tpJson)).getAsJsonObject();
            JsonArray topLevelTopics = rootTp.get("children").getAsJsonArray();
            List<Topic> processingTopics = new ArrayList<>();
            int count = 0;
            for (JsonElement tmp : topLevelTopics) {
                JsonObject topLevelTopic = tmp.getAsJsonObject();
                Topic topic = processJsonTopic(count++, topLevelTopic);
                processingTopics.add(topic);
                System.out.println(topic.getId());
            }
            LOG.log(Level.INFO, "found {0} topics", processingTopics.size());
            // here we have all topics (including their top terms)
            // now we need to create the cell topic relations
            JsonArray arrayUtr = new JsonParser().parse(new FileReader(utrJson)).getAsJsonArray();
            Map<FilterCell, Map<Topic, Double>> cellTopicProbs = getCellTopicProbabilities(processingTopics, arrayUtr);

            Set<Topic> usedTopics = new HashSet<>();
            for (Map.Entry<FilterCell, Map<Topic, Double>> entrySet : cellTopicProbs.entrySet()) {
                usedTopics.addAll(createRelevantTopicCellMappings(processingTopics, entrySet.getValue(), entrySet.getKey()));
            }
            LOG.log(Level.INFO, "used {0} topics", usedTopics.size());
            this.topics = ImmutableList.copyOf(usedTopics);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(IHTMTopicExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        new IHTMTopicExtractor(null, null, null, null, null, null).computeTopics();
    }
}
