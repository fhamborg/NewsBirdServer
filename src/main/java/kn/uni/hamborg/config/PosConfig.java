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
public class PosConfig {

    private static final Logger LOG = Logger.getLogger(PosConfig.class.getSimpleName());

    private static final String prefix = "/pos";

    public static final File modelEnglishPOSTagger = new File(FileConfig.basePathMpProjectModels + prefix, "wsj-0-18-bidirectional-distsim.tagger");
}
