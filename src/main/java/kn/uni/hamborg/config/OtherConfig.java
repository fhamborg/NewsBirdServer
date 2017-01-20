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
public class OtherConfig {

    private static final Logger LOG = Logger.getLogger(OtherConfig.class.getSimpleName());

    private static final File basepath = new File(FileConfig.basePathMpProject, "other");

    public static final File translationProgress = new File(basepath, "translationprogress.txt");
}
