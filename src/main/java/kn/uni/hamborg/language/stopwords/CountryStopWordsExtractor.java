/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.stopwords;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.LanguageConfig;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.utils.IndexUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;

/**
 * Extracts for each pubCountry the most frequent words. Note that this differs
 * from selecting the same country in the ERA GUI and looking for the right
 * summarization terms. This is because ERA GUI summarization terms are TTF IDF
 * whereas this CoutnryStopWordsExtractor is based on TTF only.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CountryStopWordsExtractor {

    private static final Logger LOG = Logger.getLogger(CountryStopWordsExtractor.class.getSimpleName());

    /**
     * See 150407_country stop words: 10 seems to be a good choice.
     */
    private static final int numberOfCountryStopWords = 10;

    private static final int numberOfWorldStopWords = 100;

    private final Directory directory;
    private final IndexReader indexReader;
    private final IndexSearcher indexSearcher;

    public CountryStopWordsExtractor() {
        try {
            this.directory = IndexUtils.openDirectory(LuceneConfig.INDEX_DIR_DEFAULT);
            this.indexReader = IndexUtils.createIndexReader(directory);
            this.indexSearcher = IndexUtils.createIndexSearcher(indexReader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Multiset<String> countInIndex() throws IOException {
        final Multiset<String> termCounts = HashMultiset.create();

        Terms terms = MultiFields.getTerms(indexReader, LightDoc.CONTENT);
        TermsEnum termsEnum = terms.iterator(null);
        BytesRef term = null;
        while ((term = termsEnum.next()) != null) {
            termCounts.add(term.utf8ToString(), safeToInt(termsEnum.totalTermFreq()));
        }
        return termCounts;
    }

    private static int safeToInt(long l) {
        if (l < Integer.MAX_VALUE) {
            return (int) l;
        } else {
            throw new RuntimeException("too large: " + l);
        }
    }

    public Multiset<String> countInDocs(int[] docids) {
        try {
            final Multiset<String> termCounts = HashMultiset.create();

            // we need to get all document ids from the new temporary index
            for (int docid : docids) {
                try {
                    Terms terms = indexReader.getTermVector(docid, LightDoc.CONTENT);
                    if (terms == null || terms.size() == 0) {
                        continue;
                    }
                    TermsEnum termsEnum = terms.iterator(null); // access the terms for this field
                    BytesRef term = null;
                    while ((term = termsEnum.next()) != null) {// explore the terms for this field
                        DocsEnum docsEnum = termsEnum.docs(null, null); // enumerate through documents, in this case only one
                        int docIdEnum;
                        while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                            termCounts.add(term.utf8ToString(), docsEnum.freq());

                        }
                    }

                } catch (IOException ex) {
                    Logger.getLogger(CountryStopWordsExtractor.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }

            //  localIndexReader.close();
            return termCounts;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String[] getAllPubCountries() {
        try {
            Set<String> countryCodes = new HashSet<>();
            Terms vector = MultiFields.getTerms(indexReader, LightDoc.PUB_COUNTRY);
            TermsEnum termsEnum = vector.iterator(null);

            BytesRef text = null;
            while ((text = termsEnum.next()) != null) {
                String term = text.utf8ToString();
                countryCodes.add(term);
            }
            System.out.println(countryCodes);
            return countryCodes.toArray(new String[0]);

        } catch (IOException ex) {
            Logger.getLogger(CountryStopWordsExtractor.class
                    .getName()).log(Level.SEVERE, null, ex);

            return null;
        }
    }

    public HashMap<String, Set<String>> extract() throws FileNotFoundException {
        final String[] pubCountries = getAllPubCountries();
        final HashMap<String, Set<String>> countryStopWords = new HashMap<>();

        for (String pubCountry : pubCountries) {
            System.out.println("analyzing " + pubCountry);

            countryStopWords.put(pubCountry, new HashSet<>());

            int[] docids = IndexUtils.getDocIDsWithExactMatch(indexSearcher, LightDoc.PUB_COUNTRY, pubCountry);
            Multiset<String> countryCount = countInDocs(docids);
            int i = numberOfCountryStopWords;
            // quick fix for Poland, because there are many bad articles inside and it will contain mostly programming language stuff if only top 10.
            if (pubCountry.equals("PL")) {
                i += 100;
            }
            for (String term : Multisets.copyHighestCountFirst(countryCount).elementSet()) {
                System.out.println(term + ": " + countryCount.count(term));
                countryStopWords.get(pubCountry).add(term);
                if (--i == 0) {
                    break;
                }
            }
            System.out.println();
        }

        SerializationUtils.serialize(countryStopWords, new FileOutputStream(LanguageConfig.countryStopWordsBinFile));
        return countryStopWords;
    }

    public List<String> extractWorld() throws IOException {
        final List<String> worldStopWords = new ArrayList<>();
        Multiset<String> worldCount = countInIndex();

        int i = numberOfWorldStopWords;
        for (String term : Multisets.copyHighestCountFirst(worldCount).elementSet()) {
            System.out.println(term + ": " + worldCount.count(term));
            worldStopWords.add(term);
            if (--i == 0) {
                break;
            }
        }
        return worldStopWords;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        CountryStopWordsExtractor e = new CountryStopWordsExtractor();
        e.extract();
        //System.out.println(e.extractWorld().toString());
    }
}
