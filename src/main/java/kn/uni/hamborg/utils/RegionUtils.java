/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.util.Locale;
import java.util.logging.Logger;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class RegionUtils {

    private static final Logger LOG = Logger.getLogger(RegionUtils.class.getSimpleName());

    /**
     * Returns a human-friendly readable name.
     *
     * @return
     */
    public String getCountryName() {
        return getCountryName(this.toString());
    }

    public static String getCountryName(final String ISO_COUNTRY) {
        Locale l = new Locale("", ISO_COUNTRY);
        return l.getDisplayCountry();
    }
}
