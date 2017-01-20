/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.knowledge;

/**
 * Defines the interface for any related term finder class.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public interface IRelatedTermsFinder {

    /**
     * Finds related terms for this given {@code term}.
     *
     * @param term
     * @return
     */
    public String[] findRelatedQueryTerms(String term);
}
