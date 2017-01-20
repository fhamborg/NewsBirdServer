/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.knowledge;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import kn.uni.hamborg.config.DBPediaConfig;
import net.sf.extjwnl.JWNLException;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class DBPediaRelatedTermsFinder implements IRelatedTermsFinder {

    private static final Logger LOG = Logger.getLogger(DBPediaRelatedTermsFinder.class.getSimpleName());

    private Map<String, Set<String>> countriesAndRelatedTerms;
    private Map<String, Set<String>> valueToCountryUrl;
    private WordNetRelatedTermsFinder wordnetFinder;

    public DBPediaRelatedTermsFinder() throws FileNotFoundException, IOException, ClassNotFoundException, JWNLException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DBPediaConfig.dbPediaExtractedExpansionTermsFile))) {
            this.countriesAndRelatedTerms = (Map<String, Set<String>>) ois.readObject();
            this.valueToCountryUrl = (Map<String, Set<String>>) ois.readObject();
            wordnetFinder = new WordNetRelatedTermsFinder();
        }

    }

    /**
     * Finds related terms for this country, e.g., its capital city, state
     * leader. For further information see {@link DBPediaExtractor}.
     *
     * @param country
     * @return
     */
    public String[] findRelatedQueryTermsRaw(String country) {
        Set<String> urls = valueToCountryUrl.get(country);
        if (urls == null) {
            return new String[]{};
        }

        Set<String> relatedTerms = new HashSet<>();

        for (String url : urls) {
            relatedTerms.addAll(countriesAndRelatedTerms.get(url));
        }
        return relatedTerms.toArray(new String[0]);
    }

    /**
     * Internally invokes {@link #findRelatedQueryTermsRaw(java.lang.String)}
     * but at first for better resolving uses WordNet, or more specifically
     * {@link WordNetRelatedTermsFinder} to get related (normalized) words. <br>
     * <br>
     * For example great britain or Great britain or Great Britain is in all
     * cases resolved with WordNet to England (among others). Thus, we can
     * easily use our look up table and find ourselves related terms.
     *
     * @param country
     * @return
     */
    @Override
    public String[] findRelatedQueryTerms(String country) {
        Set<String> relatedTerms = new TreeSet<>();
        String[] wordnetrelatedTerms = wordnetFinder.findRelatedQueryTerms(country);

        for (String wordnetrelatedTerm : wordnetrelatedTerms) {
            relatedTerms.add(wordnetrelatedTerm);
            relatedTerms.addAll(Arrays.asList(findRelatedQueryTermsRaw(wordnetrelatedTerm)));
        }
        return relatedTerms.toArray(new String[0]);
    }

    public static void main(String[] args) throws Exception {
        final DBPediaRelatedTermsFinder termFinder = new DBPediaRelatedTermsFinder();

        System.out.println(Arrays.toString(termFinder.findRelatedQueryTerms("britain")));
        System.out.println(Arrays.toString(termFinder.findRelatedQueryTerms("russia")));
        System.out.println(Arrays.toString(termFinder.findRelatedQueryTerms("germany")));
        System.out.println(Arrays.toString(termFinder.findRelatedQueryTerms("merkel")));
        System.out.println(Arrays.toString(termFinder.findRelatedQueryTerms("putin")));
        System.out.println(Arrays.toString(termFinder.findRelatedQueryTerms("obama")));
        
        /*
        System.out.println(Arrays.toString(termFinder.findRelatedQueryTermsRaw("England")));
        System.out.println(Arrays.toString(termFinder.findRelatedQueryTermsRaw("Russia")));
        System.out.println(Arrays.toString(termFinder.findRelatedQueryTermsRaw("Germany")));
        System.out.println(Arrays.toString(termFinder.findRelatedQueryTermsRaw("Angela Merkel")));
        System.out.println(Arrays.toString(termFinder.findRelatedQueryTermsRaw("Vladimir Putin")));
        System.out.println(Arrays.toString(termFinder.findRelatedQueryTermsRaw("Barack Obama")));
         */
    }

}
