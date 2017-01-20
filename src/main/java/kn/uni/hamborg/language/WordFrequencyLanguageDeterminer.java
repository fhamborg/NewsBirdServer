package kn.uni.hamborg.language;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Language-Determiner based on most frequent words of the specified language.
 * Words will be converted to lower case -> some improvement possible?
 *
 * A sentence is determined as English (or German), if it only contains latin
 * characters and at least one frequent English word.
 *
 * @author Michael Hund, University of Konstanz
 *
 */
public class WordFrequencyLanguageDeterminer implements ILanguageDeterminer {

    private static final String base = "models/languageDetection/";
//	public static final String fileEnglishWords = "data/2000mostFrequentEnglishWords.txt";
    public static final String fileEnglishWords = base + "en3000.txt";

//	public static final String fileGermanWords = "data/2000mostFrequentGermanWords.txt";
    public static final String fileGermanWords = base + "de3000.txt";

    private final Set<String> englishWords;
    private final Set<String> germanWords;

    // a pattern for to decide whether a string contains only latin characters or not
    private final Pattern latinPattern;

    public WordFrequencyLanguageDeterminer() {

        latinPattern = Pattern.compile("\\p{InBasic_Latin}*"); //only Latin characters. see: http://www.regular-expressions.info/unicode.html

        englishWords = new HashSet<>();
        germanWords = new HashSet<>();

        try {

            String line;

            //import english words
            InputStream inStream = new FileInputStream(fileEnglishWords);
            InputStreamReader inReader = new InputStreamReader(inStream);
            BufferedReader reader = new BufferedReader(inReader);

            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("//")) {
                    englishWords.add(line.toLowerCase());
                }
            }

            reader.close();
            inReader.close();
            inStream.close();

            //import german words
            inStream = new FileInputStream(fileGermanWords);
            inReader = new InputStreamReader(inStream);
            reader = new BufferedReader(inReader);

            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("//")) {
                    germanWords.add(line.toLowerCase());
                }
            }

            reader.close();
            inReader.close();
            inStream.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public boolean isEnglishSentence(String[] sentenceTokens, String sentence) {
        //check if only latin characters are used
        if (!latinPattern.matcher(sentence).matches()) {
            return false;
        }

        return countWordMatches(sentenceTokens, englishWords) > 0 ? true : false;
    }

    @Override
    public boolean isGermanSentence(String[] sentenceTokens, String sentence) {

        //check if only latin characters are used
        if (!latinPattern.matcher(sentence).matches()) {
            return false;
        }

        return countWordMatches(sentenceTokens, germanWords) > 0 ? true : false;
    }

    /**
     * Count the number of words, which are in the sentence and also in the
     * frequentWordList. Return the number.
     *
     * @param sentence the sentence which you want to analyze
     * @param frequentWords a set with the frequent words, which you use as a
     * reference
     * @return the number of matches, or -1, if one parameter is null
     */
    private int countWordMatches(final String[] sentence, Set<String> frequentWords) {

        if (sentence == null || frequentWords == null) {
            return -1;
        }

        int matches = 0;
        for (String s : sentence) {
            if (frequentWords.contains(s.toLowerCase())) {
                matches++;
//				System.out.println("frequent word: " + s);
            }
        }
        return matches;

    }

    @Override
    public boolean containsNonLatinCharacters(String sentence) {

        return !latinPattern.matcher(sentence).matches();
    }
}
