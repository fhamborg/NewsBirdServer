/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.config;

import java.io.File;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Configuration for language related implementation.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class LanguageConfig {

    private static final Logger LOG = Logger.getLogger(LanguageConfig.class.getSimpleName());

    public static final Locale DEFAULT_LOCALE = Locale.US;
    public static final DecimalFormatSymbols DEFAULT_DECIMAL_SYMBOLS = new DecimalFormatSymbols(DEFAULT_LOCALE);

    private static final File base = new File(FileConfig.basePathMpProjectModels, "language");

    public static final File berkeleyLmTempDir = new File(FileConfig.basePathMpLocalData, "berkeleyLm");

    public static final File countryStopWordsBinFile = new File(base, "countryStopWords.bin");
}
