/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.knowledge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.DBPediaConfig;
import kn.uni.hamborg.config.FileConfig;

/**
 * Provides functionality to extract required knowledge from the DBPedia files.
 * You need to download this file for extraction:
 * http://data.dws.informatik.uni-mannheim.de/dbpedia/2014/en/mappingbased_properties_cleaned_en.nt.bz2
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class DBPediaExtractor {

    private static final Logger LOG = Logger.getLogger(DBPediaExtractor.class.getSimpleName());

    Set<String> countryUrls;
    Map<String, Set<String>> countryUrlsValues;
    Map<String, Set<String>> countryUrlsValueUrls;
    Map<String, Set<String>> reverseLookUpValueUrlToCountryUrl;
    int totalValueUrlCount = 0, totalValueCount = 0;

    // Bsp: http://dbpedia.org/page/Ukraine
    final String demonym = "http://dbpedia.org/ontology/demonym";// (Einwohner, evtl mehrere?), 
    final String capital = "http://dbpedia.org/ontology/capital"; // (Hauptstadt)
    final String leader = "http://dbpedia.org/ontology/leader";// (gibt mehrere)
    final String longName = "http://dbpedia.org/ontology/longName";
    final String commonName = "http://xmlns.com/foaf/0.1/name";

    private void findValues(String findThis) throws IOException {
        LOG.info("finding values '" + findThis + "'");

        int valueCount = 0, urlCount = 0;

        boolean noValues = findThis.equals(leader);

        try (BufferedReader reader = new BufferedReader(new FileReader(DBPediaConfig.dbPediaMappingsFile))) {
            String line = reader.readLine();
            while (line != null) {
                if (line.contains(findThis)) {
                    String[] items = line.split(" ");
                    String curCountryUrl = items[0].substring(1, items[0].length() - 1);

                    Set<String> values = countryUrlsValues.get(curCountryUrl);
                    // check whether this is really a country we extracted earlier
                    // if it is null then it is probably just a region or something
                    if (values != null) {
                        int start = line.indexOf("\"") + 1;
                        int end = line.indexOf("\"", start + 1);
                        try {
                            String value = line.substring(start, end);
                            if (noValues) {
                                // even though in leader we have values, we should not use them as the names are not really telling
                                // e.g., Summit Presidency (for Portugese), thus we just use the urls which sometimes actually contain 
                                // links to real persons

                                // note that this check is after the substring, so if it really is an url we will still get an exception
                                // and come to url extraction part
                                line = reader.readLine();
                                continue;
                            }
                            values.add(value);
                            valueCount++;
                            totalValueCount++;
                        } catch (StringIndexOutOfBoundsException ss) {
                            //   we got an URL :-( from this we need to extract the name (see commonName)
                            final String newUrl = items[2].substring(1, items[2].length() - 1);
                            countryUrlsValueUrls.get(curCountryUrl).add(newUrl);
                            urlCount++;
                            totalValueUrlCount++;
                        }

                    }

                }

                line = reader.readLine();
            }
        }

        LOG.log(Level.INFO, "{0} new values", valueCount);
        LOG.log(Level.INFO, "{0} new urls", urlCount);

    }

    private void resolveUrlsToNames() throws IOException {
        int valueCount = 0, urlCount = 0;
        Set<String> allUrls = new HashSet<>();
        //System.out.println(countryUrlsValues);
        for (Map.Entry<String, Set<String>> entrySet : countryUrlsValueUrls.entrySet()) {
            Set<String> value = entrySet.getValue();
            allUrls.addAll(value);
        }
        // System.out.println("allUrls");
        // System.out.println(allUrls);

        try (BufferedReader reader = new BufferedReader(new FileReader(DBPediaConfig.dbPediaMappingsFile))) {
            // skip first line
            reader.readLine();
            String line = reader.readLine();
            while (line != null) {
                if (line.contains(commonName)) {
                    String[] items = line.split(" ");
                    String curUrl = items[0].substring(1, items[0].length() - 1);

                    // we have a name line here, let's check whether it is the name of a url
                    if (allUrls.contains(curUrl)) {
                        //System.out.println(line);
                        int start = line.indexOf("\"") + 1;
                        int end = line.indexOf("\"", start + 1);
                        try {
                            Set<String> resolvedCountryUrls = reverseLookUpValueUrlToCountryUrl.get(curUrl);
                            //System.out.println("resolved " + curUrl + " to " + resolvedCountryUrls);
                            String value = line.substring(start, end);
                            for (String resolvedCountryUrl : resolvedCountryUrls) {
                                countryUrlsValues.get(resolvedCountryUrl).add(value);
                            }
                            valueCount++;
                            totalValueCount++;

                        } catch (StringIndexOutOfBoundsException ss) {
                        }
                    }

                }

                line = reader.readLine();
            }
        }

        LOG.log(Level.INFO, "{0} new values", valueCount);

    }

    private void extractCountryUrls() throws Exception {
        final BufferedReader reader = new BufferedReader(new FileReader(DBPediaConfig.dbPediaMappingsFile));
        final String locationCountry = "dbpedia.org/ontology/locationCountry";

        // first find out all country names: dbpedia.org/ontology/locationCountry
        // results will look like this: 
        // <http://dbpedia.org/resource/CSN_International> <http://dbpedia.org/ontology/locationCountry> <http://dbpedia.org/resource/United_States> .
        countryUrls = new HashSet<>(100);
        String line = reader.readLine();
        int lineCount = 0;
        while (line != null) {
            if (line.contains(locationCountry)) {
                String url = line.split(" ")[2];
                url = url.substring(1, url.length() - 1);
                countryUrls.add(url);
            }
            line = reader.readLine();
            lineCount++;
        }
        reader.close();
        LOG.log(Level.INFO, "{0} lines", lineCount);
        LOG.log(Level.INFO, "{0} country urls", countryUrls.size());

        /*  for (String countryNameUrl : countryNameUrls) {
         Set<String> demonyms = findValues(countryNameUrl, demonym);
         System.out.println(demonyms.toString());
         }
         */
    }

    void work() throws Exception {
        final String[] valueIdentifiers = new String[]{
            demonym,
            capital,
            commonName,
            leader,
            longName
        };

        // get all the country urls
        extractCountryUrls();

        // initialize data structure which contains query expansion terms
        countryUrlsValues = new HashMap<>();
        countryUrlsValueUrls = new HashMap<>();
        countryUrls.stream().forEach((countryUrl) -> {
            countryUrlsValues.put(countryUrl, new HashSet<>());
            countryUrlsValueUrls.put(countryUrl, new HashSet<>());
        });

        for (String valueIdentifier : valueIdentifiers) {
            findValues(valueIdentifier);
        }

        // create reverse lookup map
        reverseLookUpValueUrlToCountryUrl = new HashMap<>();
        for (Map.Entry<String, Set<String>> entrySet : countryUrlsValueUrls.entrySet()) {
            String key = entrySet.getKey();
            Set<String> values = entrySet.getValue();
            for (String value : values) {
                Set<String> countryUrls = reverseLookUpValueUrlToCountryUrl.get(value);
                if (countryUrls == null) {
                    countryUrls = new HashSet<>();
                    reverseLookUpValueUrlToCountryUrl.put(value, countryUrls);
                }

                countryUrls.add(key);
            }
        }
        System.out.println(reverseLookUpValueUrlToCountryUrl);

        // now we need to go over all the countryUrlsValueUrls to get their names and add them to the first
        // done
        LOG.log(Level.INFO, "extracting names of urls ({0})", totalValueUrlCount);
        resolveUrlsToNames();

        LOG.log(Level.INFO, "found {0} expansion terms for {1} countries", new Object[]{totalValueCount, countryUrls.size()});
        //System.out.println(countryUrlsValues);
        //  System.out.println("");
        //  System.out.println(countryUrlsValueUrls);

        LOG.info("creating expansion to url mapper");
        Map<String, Set<String>> valueToCountryUrl = new HashMap<>();
        for (Map.Entry<String, Set<String>> entrySet : countryUrlsValues.entrySet()) {
            String url = entrySet.getKey();
            Set<String> values = entrySet.getValue();
            for (String value : values) {
                Set<String> newUrls = valueToCountryUrl.get(value);
                if (newUrls == null) {
                    newUrls = new HashSet<>();
                    valueToCountryUrl.put(value, newUrls);
                }
                newUrls.add(url);
            }
        }

        LOG.info("serializing to disk ");
        DBPediaConfig.dbPediaExtractedExpansionTermsFile.delete();
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DBPediaConfig.dbPediaExtractedExpansionTermsFile));
        oos.writeObject(countryUrlsValues);
        oos.writeObject(valueToCountryUrl);
        oos.close();
    }

    public static void main(String[] args) throws Exception {
        DBPediaExtractor e = new DBPediaExtractor();
        e.work();

    }
}
