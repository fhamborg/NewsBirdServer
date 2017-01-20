/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * Provides some functionality to work with or create CSV files.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CSVUtils {

    private static final Logger LOG = Logger.getLogger(CSVUtils.class.getSimpleName());

    /**
     * Writes the map to a CSV file.
     *
     * @param out
     * @param map
     */
    public static void writeToCSV(File out, Map<? extends Number, ? extends Number> map) {
        final DecimalFormat df = new DecimalFormat("#.#");
        df.setMaximumFractionDigits(8);

        final StringBuilder builder = new StringBuilder();
        for (Entry<? extends Number, ? extends Number> entrySet : map.entrySet()) {
            Number key = entrySet.getKey();
            Number value = entrySet.getValue();

            builder.append(df.format(key.doubleValue()));
            builder.append(',');
            builder.append(df.format(value.doubleValue()));
            builder.append(System.lineSeparator());
        }

        final String result = builder.toString();

        try {
            FileUtils.write(out, result);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
