/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.text.DecimalFormat;
import java.util.logging.Logger;
import static kn.uni.hamborg.config.LanguageConfig.DEFAULT_DECIMAL_SYMBOLS;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class NumberUtils {

    private static final Logger LOG = Logger.getLogger(NumberUtils.class.getSimpleName());

    public static final DecimalFormat defaultDecimalFormat = new DecimalFormat("#.#", DEFAULT_DECIMAL_SYMBOLS);

    public static final DecimalFormat detailedDecimalFormat = new DecimalFormat("#.###", DEFAULT_DECIMAL_SYMBOLS);
}
