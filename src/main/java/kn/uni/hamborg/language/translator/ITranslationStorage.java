/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.translator;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public interface ITranslationStorage {

    /**
     * Get the translation text by id.
     *
     * @param id
     * @return Null, if not present.
     */
    public String getTranslation(String id);

    /**
     * Stores the translation to the storage under the given id.
     *
     * @param id
     * @param translation
     * @throws RuntimeException If there is already something different stored
     * under id.
     */
    public void putTranslation(String id, String translation);
}
