/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.summary.synonym;

import java.util.logging.Logger;

/**
 * Represents synonyms for a cell. These can be filtered out in top tokens in
 * summarization.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
class Synonyms {

    private static final Logger LOG = Logger.getLogger(Synonyms.class.getSimpleName());

    private final String[] synonyms;

    public Synonyms(String[] synonyms) {
        this.synonyms = synonyms;
    }

    public String[] getSynonyms() {
        return synonyms;
    }

}
