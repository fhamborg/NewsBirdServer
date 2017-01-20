/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.MPQASubjectivityConfig;

/**
 * Extracts subjectivity clues (words). The file is downloaded from
 * http://mpqa.cs.pitt.edu/lexicons/subj_lexicon/.
 * <br>
 * <br>
 * Excerpt from readme (also found in project/models/...).
 *
 * Each line in the file contains one subjectivity clue. Below is an example:
 *
 * type=strongsubj len=1 word1=abuse pos1=verb stemmed1=y priorpolarity=negative
 *
 * a. type - either strongsubj or weaksubj A clue that is subjective in most
 * context is considered strongly subjective (strongsubj), and those that may
 * only have certain subjective usages are considered weakly subjective
 * (weaksubj).
 *
 * b. len - length of the clue in words All clues in this file are single words.
 *
 * c. word1 - token or stem of the clue
 *
 * d. pos1 - part of speech of the clue, may be anypos (any part of speech)
 *
 * e. stemmed1 - y (yes) or n (no) Is the clue word1 stemmed? If stemmed1=y,
 * this means that the clue should match all unstemmed variants of the word with
 * the corresponding part of speech. For example, "abuse", above, will match
 * "abuses" (verb), "abused" (verb), "abusing" (verb), but not "abuse" (noun) or
 * "abuses" (noun).
 *
 * f. priorpolarity - positive, negative, both, neutral The prior polarity of
 * the clue. Out of context, does the clue seem to evoke something positive or
 * something negative.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class MPQASubjectivityExtractor {

    private static final Logger LOG = Logger.getLogger(MPQASubjectivityExtractor.class.getSimpleName());

    private String getValue(String item) {
        return item.substring(item.indexOf("=") + 1).trim();
    }

    /**
     * For a given String {@code value} creates a corresponding Polarity enum.
     *
     * @param value
     * @return
     */
    public Subjectivity.Polarity getPolarity(String value) {
        switch (value) {
            case "positive":
                return Subjectivity.Polarity.POS;
            case "negative":
                return Subjectivity.Polarity.NEG;
            case "neutral":
                return Subjectivity.Polarity.NEUTRAL;
            case "both":
                return Subjectivity.Polarity.BOTH;
        }
        throw new IllegalArgumentException(value + " is unknown");
    }

    /**
     * For a given String {@code value} creates a corresponding Type enum.
     *
     * @param value
     * @return
     */
    public Subjectivity.Type getType(String value) {
        switch (value) {
            case "strongsubj":
                return Subjectivity.Type.STRONG;
            case "weaksubj":
                return Subjectivity.Type.WEAK;
        }
        throw new IllegalArgumentException(value + " is unknown");
    }

    private void extract() throws IOException {
        LOG.log(Level.INFO, "extracting from {0}", MPQASubjectivityConfig.inputSubjectivityCluesFile);

        final List<String> lines = Files.readAllLines(MPQASubjectivityConfig.inputSubjectivityCluesFile.toPath());
        final List<SubjectivityWord> words = new ArrayList<>(lines.size());
        final Map<String, Subjectivity.Polarity> verifyNoDifferent = new HashMap<>(lines.size());

        lines.stream().forEach((line) -> {
            String[] items = line.split(" ");
            String type = getValue(items[0]);
            String word = getValue(items[2]);
            String pos = getValue(items[3]);
            String polarity = getValue(items[5]);

            Subjectivity.Polarity p = verifyNoDifferent.get(word);
            Subjectivity.Polarity newp = getPolarity(polarity);

            if (p == null) {
                verifyNoDifferent.put(word, newp);
            } else if (p == newp) {

            } else {
                System.out.println(word + " " + newp + " --- " + p);
            }

            words.add(
                    new SubjectivityWord(getType(type), getPolarity(polarity), word, pos, SimplePos.fromMPQAString(pos))
            );

        });

        LOG.log(Level.INFO, "extracted {0} entries", words.size());

        LOG.info("building map for more efficient data access");
        Map<String, Set<SubjectivityWord>> tokenAndEntries = new HashMap<>();
        for (SubjectivityWord word : words) {
            String token = word.getWord();
            Set<SubjectivityWord> entries = tokenAndEntries.get(token);
            if (entries == null) {
                entries = new HashSet<>();
                tokenAndEntries.put(token, entries);
            }
            entries.add(word);
        }

        // write to file
        MPQASubjectivityConfig.subjectivityModel.delete();
        final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MPQASubjectivityConfig.subjectivityModel));
        oos.writeObject(words);
        oos.writeObject(tokenAndEntries);
        oos.close();

        LOG.log(Level.INFO, "written to disk as model to {0}", MPQASubjectivityConfig.subjectivityModel.getAbsolutePath());
    }

    public static void main(String[] args) throws IOException {
        MPQASubjectivityExtractor e = new MPQASubjectivityExtractor();
        e.extract();
    }
}
