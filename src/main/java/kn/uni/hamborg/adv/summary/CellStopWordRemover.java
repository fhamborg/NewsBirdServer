/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.summary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.table.FilterCell;
import kn.uni.hamborg.adv.table.TableManager;
import kn.uni.hamborg.language.stopwords.CountryStopWords;
import kn.uni.hamborg.language.stopwords.WorldStopWords;
import kn.uni.hamborg.lucene.summarizer.StringScore;

/**
 * Removes stopwords from a cell.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CellStopWordRemover {

    private static final Logger LOG = Logger.getLogger(CellStopWordRemover.class.getSimpleName());

    private final TableManager tableManager;
    private final CountryStopWords countryStopWords;

    public CellStopWordRemover(TableManager tableManager) {
        this.tableManager = tableManager;
        this.countryStopWords = new CountryStopWords();
    }

    public List<StringScore> removeStopwords(FilterCell cell, List<StringScore> words) {
        String countryCode = tableManager.getCountryCode(cell);
        Set<String> stopwords = countryStopWords.getStopWords(countryCode);

        Set<String> tmp = new HashSet<>(stopwords);
        tmp.addAll(WorldStopWords.STOPWORDSET);

        List<StringScore> cleanedWords = new ArrayList<>();

        for (StringScore ss : words) {
            if (!tmp.contains(ss.getValue())) {
                cleanedWords.add(ss);
            }
        }
        return cleanedWords;
    }
}
