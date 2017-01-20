/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.translator;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.util.DbDump;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.TranslatorConfig;

/**
 * Uses Oracle Berkeley DB to store and read translated texts.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TranslationStorageDB implements ITranslationStorage {

    private static final Logger LOG = Logger.getLogger(TranslationStorageDB.class.getSimpleName());
    private static final Charset charset = Charset.forName("UTF-8");

    private static TranslationStorageDB INSTANCE = null;

    private final Database translationDB;
    private final Environment myDbEnvironment;

    private TranslationStorageDB() {
        try {
            // Open the environment, creating one if it does not exist
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);
            myDbEnvironment = new Environment(TranslatorConfig.translationDB, envConfig);

            // Open the database, creating one if it does not exist
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setDeferredWrite(true);
            translationDB = myDbEnvironment.openDatabase(null,
                    "translationDB", dbConfig);

            LOG.log(Level.INFO, "opened DB {0} with {1} entries",
                    new Object[]{
                        TranslatorConfig.translationDB,
                        translationDB.count()});

        } catch (DatabaseException dbe) {
            //  Exception handling
            throw new RuntimeException(dbe);
        }
    }

    public static TranslationStorageDB getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TranslationStorageDB();
        }
        return INSTANCE;
    }

    public void close() {
        try {
            translationDB.close();
            myDbEnvironment.close();
        } catch (DatabaseException ex) {
            Logger.getLogger(TranslationStorageDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void dump(File where) {
        try {
            DbDump dump = new DbDump(myDbEnvironment, "translationDB", new PrintStream(where), true);
            dump.dump();
        } catch (IOException | DatabaseException ex) {
            Logger.getLogger(TranslationStorageDB.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        try {
            DatabaseEntry theKey = new DatabaseEntry(id.getBytes(charset));
            DatabaseEntry theData = new DatabaseEntry(translation.getBytes(charset));
            translationDB.put(null, theKey, theData);
            translationDB.sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the translation text by id.
     *
     * @param id
     * @return Null, if not present.
     */
    public String getTranslation(String id) {
        try {
            DatabaseEntry theKey = new DatabaseEntry(id.getBytes(charset));
            DatabaseEntry theData = new DatabaseEntry();

            // Call get() to query the database
            if (translationDB.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {

                // Translate theData into a String.
                byte[] retData = theData.getData();
                String foundData = new String(retData, charset);
                //System.out.println("key: '" + id + "' data: '" + foundData + "'.");
                return foundData;
            } else {
                //System.out.println("No record found with key '" + id + "'.");
            }
        } catch (DatabaseException de) {

        }
        return null;
    }

    /**
     *
     * @param id
     * @return True if an entry with the id already exists. Else false.
     */
    public boolean contains(String id) {
        return getTranslation(id) != null;
    }

    private void convert() throws DatabaseException {
        TranslationStorage ts = TranslationStorage.getInstance();
        Set<String> ids = ts.getAllIds();
        System.out.println("" + ids.size() + " ids");

        long realstart = new Date().getTime();
        long start = realstart;
        int i = 0;
        for (String id : ids) {
            TranslationStorageDB.getInstance().putTranslation(id, ts.getTranslation(id));
            if (i++ % 1000 == 0) {
                System.out.println("" + TranslationStorageDB.getInstance().translationDB.count() + " in " + (new Date().getTime() - start));
                start = new Date().getTime();
            }
        }
        System.out.println("" + TranslationStorageDB.getInstance().translationDB.count() + " in " + (new Date().getTime() - realstart));
    }

    public static void main(String[] args) throws DatabaseException {
       // TranslationStorageDB.getInstance().convert();
        TranslationStorageDB.getInstance().dump(new File("./data/largedump.txt"));

        /*System.out.println(TranslationStorageDB.getInstance().getTranslation("felix"));
         //  TranslationStorageDB.getInstance().putTranslation("felix", "hamborg");
         System.out.println(TranslationStorageDB.getInstance().getTranslation("felix"));
         //TranslationStorageDB.getInstance().putTranslation("felix", "hamborg");
         //TranslationStorageDB.getInstance().putTranslation("felix", "invalid");

         long start = new Date().getTime();
         for (int i = 0; i < 1000000; i++) {

         TranslationStorageDB.getInstance().putTranslation("id" + i, "lol");

         if (i % 1000 == 0) {
         System.out.println("" + TranslationStorageDB.getInstance().translationDB.count() + " in " + (new Date().getTime() - start));
         start = new Date().getTime();
         }

         }
         */
    }
}
