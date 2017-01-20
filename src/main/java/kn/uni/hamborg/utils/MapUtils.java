/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.topic.Topic;
import kn.uni.hamborg.adv.topic.TopicScore;
import kn.uni.hamborg.lucene.summarizer.StringScore;

/**
 * Provides nice functions to work with {@link Map}s.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class MapUtils {

    private static final Logger LOG = Logger.getLogger(MapUtils.class.getSimpleName());

    /**
     * Returns a {@link SortedSet} that contains the entries from {@code map}
     * sorted by their values.
     *
     * @param <K>
     * @param <V>
     * @param map
     * @param isDescending
     * @return
     */
    public static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
            final Map<K, V> map,
            final boolean isDescending) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        int res = e1.getValue().compareTo(e2.getValue());
                        if (isDescending) {
                            res *= -1;
                        }
                        return res != 0 ? res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());

        return sortedEntries;
    }

    public static void sortByTopicScores(final List<TopicScore> topicScores, final boolean isDescending) {
        Collections.sort(topicScores);
        if (isDescending) {
            Collections.reverse(topicScores);
        }
    }

    public static void sortByObjectScores(final List<? extends ObjectScore> objectScores, final boolean isDescending) {
        Collections.sort(objectScores);
        if (isDescending) {
            Collections.reverse(objectScores);
        }
    }

    public static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> entriesSortedByValuesAsList(final Map<K, V> map,
            final boolean isDescending) {
        List<Map.Entry<K, V>> sortedList = new ArrayList<>();
        sortedList.addAll(entriesSortedByValues(map, isDescending));
        return sortedList;
    }

    public static List<TopicScore> mapToTopicScoreList(Map<Topic, Double> map) {
        List<TopicScore> tmp = new ArrayList<>();
        for (Map.Entry<Topic, Double> entrySet : map.entrySet()) {
            Topic key = entrySet.getKey();
            Double value = entrySet.getValue();
            tmp.add(new TopicScore(key, value));
        }
        return tmp;
    }

    public static List<StringScore> mapToStringScoreList(Map<String, Double> map) {
        List<StringScore> tmp = new ArrayList<>();
        for (Map.Entry<String, Double> entrySet : map.entrySet()) {
            String key = entrySet.getKey();
            Double value = entrySet.getValue();
            tmp.add(new StringScore(key, value));
        }
        return tmp;
    }

    public static Map<String, Double> mergeMapsAndSumValuesForEqualKeys(
            Map<String, Double> map1, Map<String, Double> map2) {
        Map<String, Double> objectValues = new HashMap<>();
        Set<String> remaining2 = new HashSet<String>(map2.keySet());

        for (String o1 : map1.keySet()) {
            Double val1 = map1.get(o1).doubleValue();
            Double val2 = map2.get(o1) != null ? map2.get(o1).doubleValue() : 0.0;
            objectValues.put(o1, (val1 + val2));

            remaining2.remove(o1);
        }

        // now in remaining2 we only have those Objects of map2 left that are not in map1
        for (String o2 : remaining2) {
            Double val2 = map2.get(o2).doubleValue();
            objectValues.put(o2, val2);
        }

        return objectValues;
    }

}
