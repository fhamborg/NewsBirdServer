/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.stopwords;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import kn.uni.hamborg.config.LanguageConfig;
import org.apache.commons.lang3.SerializationUtils;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CountryStopWords {

    private static final Logger LOG = Logger.getLogger(CountryStopWords.class.getSimpleName());
    private final HashMap<String, Set<String>> countryStopWords;

    public CountryStopWords() {
        try {
            countryStopWords = (HashMap<String, Set<String>>) SerializationUtils.deserialize(new FileInputStream(LanguageConfig.countryStopWordsBinFile));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get the stop words for the country. Or an empty list.
     *
     * @param countryCode
     * @return
     */
    public Set<String> getStopWords(String countryCode) {
        return countryStopWords.getOrDefault(countryCode, new HashSet<>());
    }

}
