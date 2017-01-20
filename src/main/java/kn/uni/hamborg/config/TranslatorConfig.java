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
public class TranslatorConfig {

    private static final Logger LOG = Logger.getLogger(TranslatorConfig.class.getSimpleName());

    private static final File base = new File(FileConfig.basePathMpProjectData, "translation");

    public static final File translationDirectory = new File(base, "translationdirectory");
    public static final File translationDB = new File(base, "translationdb");
}
