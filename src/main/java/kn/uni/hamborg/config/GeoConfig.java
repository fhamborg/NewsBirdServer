/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.config;

import java.io.File;
import java.util.logging.Logger;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class GeoConfig {

    private static final Logger LOG = Logger.getLogger(GeoConfig.class.getSimpleName());

    private static final File base = new File(FileConfig.basePathMpProjectModels, "geo");

    public static final File countryCoordsFile = new File(base, "country_latlon.csv");
}
