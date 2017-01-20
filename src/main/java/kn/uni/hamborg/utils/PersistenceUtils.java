/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides basic but easy access to serialization API of java to persist and
 * load POJOs.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class PersistenceUtils {

    private static final Logger LOG = Logger.getLogger(PersistenceUtils.class.getSimpleName());

    /**
     * Persists {@code what} to {@code where}.
     *
     * @param where
     * @param what
     */
    public static void persistObject(File where, Object what) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(where));
            oos.writeObject(what);
            oos.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                oos.close();
            } catch (IOException ex) {
                Logger.getLogger(PersistenceUtils.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Loads a single object from {@code from}.
     *
     * @param from
     * @return
     */
    public static Object loadObject(File from) {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(from));
            return ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(PersistenceUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(PersistenceUtils.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }
    }
}
