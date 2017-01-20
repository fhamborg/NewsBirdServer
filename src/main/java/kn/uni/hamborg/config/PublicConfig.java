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
public class PublicConfig {

    private static final Logger LOG = Logger.getLogger(PublicConfig.class.getSimpleName());

    public static final File publicBase = new File(FileConfig.basePathMpProjectData, "public");
}
