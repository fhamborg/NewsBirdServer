/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.config;

import java.io.File;
import java.util.logging.Logger;

/**
 * File paths for OpenNLP model files.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class OpenNLPConfig {

    private static final Logger LOG = Logger.getLogger(OpenNLPConfig.class.getSimpleName());

    public static final File basePath = new File(FileConfig.basePathMpProjectModels + "opennlpmodels/");
}
