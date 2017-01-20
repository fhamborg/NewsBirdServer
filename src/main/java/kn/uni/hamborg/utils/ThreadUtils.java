/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ThreadUtils {

    private static final Logger LOG = Logger.getLogger(ThreadUtils.class.getSimpleName());

    public static boolean sleep(int ms) {
        try {
            Thread.sleep(ms);
            return true;
        } catch (InterruptedException ex) {
            Logger.getLogger(ThreadUtils.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
