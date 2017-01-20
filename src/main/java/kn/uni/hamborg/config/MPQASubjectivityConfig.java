/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.config;

import java.io.File;
import java.util.logging.Logger;

/**
 * Holds information about the MPQA based subjectivity paths.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class MPQASubjectivityConfig {

    private static final Logger LOG = Logger.getLogger(MPQASubjectivityConfig.class.getSimpleName());

    private static final String modelDirName = "subjectivity";

    public static final File inputSubjectivityCluesFile = new File(FileConfig.basePathMpProjectModels + modelDirName, "subjclueslen1-HLTEMNLP05.tff");

    public static final File subjectivityModel = new File(FileConfig.basePathMpProjectModels + modelDirName, "subjectivity-clues.bin");
}
