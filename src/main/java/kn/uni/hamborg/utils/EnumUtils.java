/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EnumUtils {

    private static final Logger LOG = Logger.getLogger(EnumUtils.class.getSimpleName());

    public static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.toString(e.getEnumConstants()).replaceAll("^.|.$", "").split(", ");
    }
}
