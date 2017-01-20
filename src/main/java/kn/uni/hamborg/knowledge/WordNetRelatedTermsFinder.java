/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.knowledge;

import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * Provides functionality to find similar and related terms for given term(s).
 * This can be used for example for query expansion.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class WordNetRelatedTermsFinder implements IRelatedTermsFinder {

    private static final Logger LOG = Logger.getLogger(WordNetRelatedTermsFinder.class.getSimpleName());

    private final Dictionary wordnetDictionary;

    public WordNetRelatedTermsFinder() throws JWNLException {
        wordnetDictionary = Dictionary.getDefaultResourceInstance();
    }

    private void test() throws JWNLException {
        Dictionary d = wordnetDictionary;
        IndexWord word = d.lookupIndexWord(POS.NOUN, "cat");
        PointerTargetTree hyponyms = PointerUtils.getHyponymTree(word.getSenses().get(0));
        System.out.println("Hyponyms of \"" + word.getLemma() + "\":");
        hyponyms.print();

        PointerTargetNodeList hypernyms = PointerUtils.getDirectHypernyms(word.getSenses().get(0));
        System.out.println("Direct hypernyms of \"" + word.getLemma() + "\":");
        hypernyms.print();

        PointerTargetTree meronyms = PointerUtils.getInheritedMeronyms(word.getSenses().get(0));
        System.out.println("Inherited meronyms of \"" + word.getLemma() + "\":");
        meronyms.print();

        // THIS IS WHAT WE ARE INTERESTED IN for expansions
        PointerTargetNodeList directmeronyms = PointerUtils.getMeronyms(word.getSenses().get(0));
        System.out.println("Direct meronyms of \"" + word.getLemma() + "\":");
        directmeronyms.print();

        // THIS IS WHAT WE MIGHT ALSO NEED, COULD BE EVEN BETTER THAN MERONYMS, SEE 'dog' 
        // (meronyms show stuff that is part of a dog, whereas word.getSenses().get(0) contains a lot of good words.
        // but in context of countries and so on probably both combined make sense
        System.out.println("Direct synonyms of \"" + word.getLemma() + "\":");
        for (Synset ss : word.getSenses()) {
            System.out.println(ss.toString());
        }
        System.out.println("fin");
        PointerUtils.getSynonyms(word.getSenses().get(0)).print();
    }

    /**
     * Finds related terms for a given {@code term}. For related query terms we
     * use WordNet's direct meronyms and synonyms. WordNet actually also needs
     * the {@code term}'s POS, and since we don't give it, we just use it with
     * all possible POSs. Also returns the term itself.
     * <br>
     * <br>
     * Die Meronymie (auch: Teil-Ganzes-Beziehung) ist eine paradigmatische
     * „hierarchische“ semantische Relation zwischen Lexemen (Wörtern,
     * Begriffen), die darauf beruht, dass ein Lexem etwas bezeichnet, was Teil
     * eines anderen („Ganzen“) ist, das von einem anderen Lexem bezeichnet
     * wird.
     *
     * @param term
     * @return
     */
    @Override
    public String[] findRelatedQueryTerms(String term) {
        try {
            IndexWord[] words = wordnetDictionary.lookupAllIndexWords(term).getIndexWordArray();

            // get the meronyms (which are "part of this")
            LinkedHashSet<String> relatedTerms = new LinkedHashSet<>();
            relatedTerms.add(term);
            for (IndexWord indexedWord : words) {
                PointerTargetNodeList directmeronyms = PointerUtils.getMeronyms(indexedWord.getSenses().get(0));
                for (PointerTargetNode directmeronym : directmeronyms) {
                    // System.out.println(directmeronym.toString());
                    //System.out.println(directmeronym.getSynset().getWords().get(0).getLemma());
                    relatedTerms.add(directmeronym.getSynset().getWords().get(0).getLemma());
                }

                // also get synonyms (which are different terms with similar/same meaning)
                for (Word word : indexedWord.getSenses().get(0).getWords()) {
                    // System.out.println(word.getLemma());
                    relatedTerms.add(word.getLemma());
                }
            }

            return relatedTerms.toArray(new String[0]);
        } catch (JWNLException ex) {
            Logger.getLogger(WordNetRelatedTermsFinder.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Convenience method for meronym and synonym based query expansion for a
     * single {@code term}. Invokes internally {@link #findRelatedQueryTerms(java.lang.String)
     * } and then {@link #luceneQueryStringExpansion(java.lang.String[]) }.
     *
     * @param term
     * @return
     */
    private String getExpandedQueryStringForTerm(String term) {
        String[] terms = findRelatedQueryTerms(term);
        String result = luceneQueryStringExpansion(terms);
        System.out.println(result);
        return result;
    }

    /**
     * Expands the given term with all of its expansionTerms. Should only be
     * used so that in case of changing something of the string concatenation
     * there will be only one place in the program where we need to change this.
     *
     * @param expansionTerms
     * @return
     */
    private String luceneQueryStringExpansion(String[] expansionTerms) {
        final StringBuilder sb = new StringBuilder("\"");
        for (String expansionTerm : expansionTerms) {
            sb.append(expansionTerm).append("\" \"");
        }
        return sb.substring(0, sb.length() - 2);
    }

    public static void main(String[] args) throws JWNLException {
        new WordNetRelatedTermsFinder().getExpandedQueryStringForTerm("germany");
        new WordNetRelatedTermsFinder().getExpandedQueryStringForTerm("dog");
        new WordNetRelatedTermsFinder().getExpandedQueryStringForTerm("cat");
        new WordNetRelatedTermsFinder().getExpandedQueryStringForTerm("hamborg");
        new WordNetRelatedTermsFinder().getExpandedQueryStringForTerm("new york");
        new WordNetRelatedTermsFinder().getExpandedQueryStringForTerm("britain");
          new WordNetRelatedTermsFinder().getExpandedQueryStringForTerm("obama");
        // new WordNetRelatedTermsFinder().test();
    }
}
