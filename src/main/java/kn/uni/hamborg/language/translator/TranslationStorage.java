/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.translator;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.TranslatorConfig;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * Central place to store translated texts. Developer needs to make sure, that
 * the IDs are really unique.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TranslationStorage implements ITranslationStorage {

    private static final Logger LOG = Logger.getLogger(TranslationStorage.class.getSimpleName());

    private static TranslationStorage instance = null;

    private final DB db;
    private final Map<String, String> translationMap;
    private final String translationMapName = "translationMap";

    private TranslationStorage() {
        db = DBMaker.newFileDB(TranslatorConfig.translationDirectory)
                .mmapFileEnable()
                .make();
        translationMap = db.getHashMap(translationMapName);

        LOG.log(Level.INFO, "opened DB {0} with {1} entries", new Object[]{TranslatorConfig.translationDirectory, db.getAll().size()});
        LOG.log(Level.INFO, "opened {0} with {1} entries", new Object[]{translationMapName, translationMap.size()});
    }

    public static TranslationStorage getInstance() {
        if (instance == null) {
            instance = new TranslationStorage();
        }
        return instance;
    }

    /**
     * Get the translation text by id.
     *
     * @param id
     * @return Null, if not present.
     */
    public String getTranslation(String id) {
        return translationMap.get(id);
    }

    /**
     *
     * @param id
     * @return True if an entry with the id already exists. Else false.
     */
    public boolean contains(String id) {
        return translationMap.containsKey(id);
    }

    /**
     * Stores the translation to the storage under the given id.
     *
     * @param id
     * @param translation
     * @throws RuntimeException If there is already something different stored
     * under id.
     */
    public void putTranslation(String id, String translation) {
        final String old = getTranslation(id);
        // check if the storage already knows this id.
        if (old != null) {
            // check if the old, stored value is the same as the one which is about to be stored.
            if (old.equals(translation)) {
                return;
            } else { // if not, throw exception
                throw new RuntimeException("Entry already exists with id '" + id + "'. "
                        + "Old: '" + old + "'. New: '" + translation + "'.");
            }
        }

        // store it
        translationMap.put(id, translation);
        db.commit();
    }

    public Set<String> getAllIds() {
        return translationMap.keySet();
    }

    public static void main(String[] args) {
        System.out.println(TranslationStorage.getInstance().getTranslation("felix"));
        TranslationStorage.getInstance().putTranslation("felix", "hamborg");
        System.out.println(TranslationStorage.getInstance().getTranslation("felix"));
        TranslationStorage.getInstance().putTranslation("felix", "hamborg");
        TranslationStorage.getInstance().putTranslation("felix", "invalid");
    }
}
