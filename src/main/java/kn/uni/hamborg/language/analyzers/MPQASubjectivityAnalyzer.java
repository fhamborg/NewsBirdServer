/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import edu.stanford.nlp.ling.CoreLabel;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.MPQASubjectivityConfig;
import kn.uni.hamborg.config.StatConfig;
import kn.uni.hamborg.language.Language;
import kn.uni.hamborg.utils.CoreLabelUtils;
import kn.uni.hamborg.utils.PersistenceUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class MPQASubjectivityAnalyzer {

    private static final Logger LOG = Logger.getLogger(MPQASubjectivityAnalyzer.class.getSimpleName());

    private final POSAnalyzer posAnalyzer;
    private final List<SubjectivityWord> words;
    private final Map<String, Set<SubjectivityWord>> tokenAndEntries;
    private final SentenceSplitter sentenceSplitter;
    private final Map<String, AggregatedSubjectivity> countryCodeSubjectivity;
    private final Map<String, AggregatedSubjectivity> publisherSubjectivity;
    private final Map<String, AggregatedSubjectivity> dayDateSubjectivity;

    /**
     * List of negation words, taken from
     * http://www.grammarly.com/handbook/sentences/negatives/
     */
    private static final String[] negationWords = new String[]{
        "no", "not", "none", "no one", "nobody", "nothing", "neither", "nowhere", "never",
        "don't", "doesn't", "isn't", "wasn't", "weren't", "shouldn't", "wouldn't", "couldn't", "won't", "can't", "cannot",};
    /**
     * If a negated word is found, the next 4 words are negated (including the
     * 4-th, 0-index based)
     */
    private static final int nextWordsAfterNegation = 4;

    public MPQASubjectivityAnalyzer() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(MPQASubjectivityConfig.subjectivityModel));
            words = (List<SubjectivityWord>) ois.readObject();
            tokenAndEntries = (Map<String, Set<SubjectivityWord>>) ois.readObject();
            ois.close();

            posAnalyzer = new POSAnalyzer();
            sentenceSplitter = new SentenceSplitter(Language.EN);

            LOG.info("reading subjectivity models: country, publisher");
            countryCodeSubjectivity = (Map<String, AggregatedSubjectivity>) PersistenceUtils.loadObject(StatConfig.pathSubjectivityModelCountry);
            publisherSubjectivity = (Map<String, AggregatedSubjectivity>) PersistenceUtils.loadObject(StatConfig.pathSubjectivityModelPublisher);
            dayDateSubjectivity = (Map<String, AggregatedSubjectivity>) PersistenceUtils.loadObject(StatConfig.pathSubjectivityModelDayDate);

            LOG.log(Level.INFO, "{0}" + "successfully created instance", MPQASubjectivityAnalyzer.class.getSimpleName());
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(MPQASubjectivityAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Finds the subjectivewords which has before that been extracted by
     * {@link MPQASubjectivityExtractor} from the MPQA subjectivity lexicon. To
     * do so we find the correct word and correct POS.
     *
     * If noPosTag = true, then just the first subjectivityword for a token is
     * returned, not checking the POS
     *
     * @param coreLabel
     * @return
     */
    private SubjectivityWord findSubjectivityWord(CoreLabel coreLabel, boolean noPosTag) {
        String token = CoreLabelUtils.getToken(coreLabel);
        Set<SubjectivityWord> subjectivityWords = tokenAndEntries.get(token);
        if (subjectivityWords == null || subjectivityWords.size() == 0) {
            return null;
        }

        if (noPosTag) {
            return subjectivityWords.iterator().next();
        }

        SimplePos pos = CoreLabelUtils.getSimplePosFromCoreLabelPos(CoreLabelUtils.getPos(coreLabel));

        for (SubjectivityWord subjectivityWord : subjectivityWords) {
            // return the subjectivity word if it has the same POS as the requested {@code coreLabel}
            // or return it if it has ANYPOS; in that case the requested POS does not matter            
            if (subjectivityWord.getSimplePos() == pos
                    || subjectivityWord.getSimplePos() == SimplePos.anypos) {
                return subjectivityWord;
            }
        }

        return null;
    }

    /**
     * Aggregates the subjectivity for multiple sentences. Therefore,
     * {@code sentences} is split into separate sentences and for each of those
     * the subjectivity is calculated.
     *
     * @param sentences
     * @return
     */
    public AggregatedSubjectivity calcSubjectivityForSentences(
            String sentences) {
        String[] splitSentences = sentenceSplitter.splitSentences(sentences);
        AggregatedSubjectivity as = new AggregatedSubjectivity();
        for (String splitSentence : splitSentences) {
            as.aggregateWith(calcSubjectivity(splitSentence, true));
        }
        return as;
    }

    /**
     * Calculates the subjectivity of the sentence. Can only be applied to ONE
     * sentence because of negation checks (if a sentence contains a negation
     * word the next sentence could also be partially negated by that).
     *
     * Similar to Podushko, 2014 (Diplomarbeit) we check whether we have a
     * negation word. If so, all following {@code } are negated in their
     * subjectivity polarity.
     *
     * @param sentence
     * @param noPosTagging If true, the analysis will be much faster but less
     * precise. Just the first SubjectivityWord will be taken for a given token,
     * not checking whether POS is correct.
     * @return
     */
    public AggregatedSubjectivity calcSubjectivity(String sentence, boolean noPosTagging) {
        int countSubjPos = 0, countSubjNeg = 0;
        final AggregatedSubjectivity aggrSubjectivity = new AggregatedSubjectivity();

        List<CoreLabel> labels = noPosTagging
                ? posAnalyzer.tokenize(sentence)
                : posAnalyzer.tagSentence(sentence);
        int negCounter = -1;
        for (CoreLabel label : labels) {
            String word = CoreLabelUtils.getToken(label);
            // check if the word is a negation word
            if (ArrayUtils.contains(negationWords, word)) {
                negCounter = nextWordsAfterNegation;
            }

            // increment token count
            aggrSubjectivity.incrementTokens();

            SubjectivityWord subjectivityWord = findSubjectivityWord(label, noPosTagging);

            if (subjectivityWord != null) {
                // System.out.println(subjectivityWord);
                int weight = subjectivityWord.getType() == Subjectivity.Type.STRONG ? 3 : 1;

                Subjectivity.Polarity polarity = subjectivityWord.getPolarity();
                if (negCounter >= 0) {
                    // System.out.println("negating (" + negCounter + ")");
                    // System.out.println(subjectivityWord.getWord());

                    polarity = polarity.inverse();
                }

                switch (polarity) {
                    case BOTH:
                        countSubjPos += weight;
                        countSubjNeg += weight;
                        break;
                    case POS:
                        countSubjPos += weight;
                        break;
                    case NEG:
                        countSubjNeg += weight;
                        break;
                }
                negCounter--;

                // increment corresponding type
                switch (subjectivityWord.getType()) {
                    case STRONG:
                        aggrSubjectivity.incrementStrongSubjectivity();
                        break;
                    case WEAK:
                        aggrSubjectivity.incrementWeakSubjectivity();
                        break;
                }
            }
        }

        //LOG.log(Level.INFO, "{0} - {1}", new Object[]{countSubjPos, countSubjNeg});
        final float ratioThreshold = 1.7f;
        final int minBothThreshold = 2;

        Subjectivity.Polarity polarity = Subjectivity.Polarity.NEUTRAL;
        if (countSubjPos > ratioThreshold * countSubjNeg) {
            polarity = Subjectivity.Polarity.POS;
        } else if (countSubjNeg > ratioThreshold * countSubjPos) {
            polarity = Subjectivity.Polarity.NEG;
        } else if (countSubjPos + countSubjNeg > minBothThreshold) {
            polarity = Subjectivity.Polarity.BOTH;
        }

        //  return new Subjectivity(polarity);
        //System.out.println(aggrSubjectivity.toString());
        return aggrSubjectivity;
    }

    public AggregatedSubjectivity getSubjectivityOfCountry(String countryCode) {
        return countryCodeSubjectivity.get(countryCode);
    }

    public AggregatedSubjectivity getSubjectivityOfPublisher(String publisher) {
        return publisherSubjectivity.get(publisher);
    }

    /**
     * {@code ymd} needs to be in {@link DateTimeUtil#simpleDateTimeFormatter}.
     *
     * @param ymd
     * @return
     */
    public AggregatedSubjectivity getSubjectivityOfDayDate(String ymd) {
        return dayDateSubjectivity.get(ymd);
    }

    public static void main(String[] args) {
        MPQASubjectivityAnalyzer a = new MPQASubjectivityAnalyzer();
        //a.calcSubjectivity("The Arguing Lexicon is available for download. The lexicon includes patterns that represent arguing. Each file (17 out of 22) represents a type (category) of arguing discussed in (Somasundaran, et al., 2007). Please refer to the README of the archive and the paper for more details");
        //  a.calcSubjectivity("Many nations on Tuesday are commemorating the crimes of the Holocaust by Nazi Germany. An international gathering at the Auschwitz death camp in Poland is missing the leader of the nation, whose predecessor liberated its prisoners 70 years ago.");
        System.out.println(a.calcSubjectivity("‘I don’t trust US’: Fidel Castro breaks silence on Cuba-America reconciliation", true).toString());
        System.out.println(a.calcSubjectivity("‘I don’t trust US’: Fidel Castro breaks silence on Cuba-America reconciliation", false).toString());
        /*F a.calcSubjectivity("An Islamist group in Denmark has hit out at a local policy to de-radicalize Muslim youths. It cites “widespread depression, addiction… and alarming rates of suicide” as proof it’s really “sad Western culture” that is in dire need of help.\n"
         + "\n"
         + "The scornful statement, which includes allusions to a “sad capitalist existential void,” was printed on the website for the group, called Hizb-ut-Tahrir. It’s a massive organization with branches in 40 countries and a regional chapter in Copenhagen.\n"
         + "\n"
         + "There is no consensus in Hizb-ut-Tahrir worldwide on support or condemnation of terrorism, as both have taken place. The group is banned in Russia and some of its activities are proscribed in a number of Muslims countries.\n"
         + "\n"
         + "Its statement comes weeks after a Copenhagen municipality decided on a plan to tackle Islamic radicalism at home.\n"
         + "\n"
         + "It plans to do this over a period of four years, with the help of external experts working under the guidance of Sweden’s top expert on the matter.\n"
         + "\n"
         + "The Sunday statements, made by group spokesman and Danish convert Junes Kock, see this as nothing but “manipulation” and “deception,” as well as an attempt to polarize the Muslim community and pigeonhole it into moderate and extreme categories. Kock believes that a plan of this sort intends to take things that are the staple of Islamic life – “a beard, prayer, scarf and general compliance with Islamic behavior” – and label them as radical in an agenda of stigmatization. ");
         a.calcSubjectivity("doesn't don't");
         a.calcSubjectivity("");
         a.calcSubjectivity("");
         a.calcSubjectivity("");*/

    }
}
