/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.config;

import java.io.File;
import java.util.logging.Logger;

/**
 * Holds information about DBPedia.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class DBPediaConfig {

    private static final Logger LOG = Logger.getLogger(DBPediaConfig.class.getSimpleName());

    private static final File dbPediaDirLocal = new File(FileConfig.basePathMpLocal, "dbpedia");
    private static final File dbPediaDirProject = new File(FileConfig.basePathMpProjectModels, "dbpedia");

    public static final File dbPediaMappingsFile = new File(dbPediaDirLocal, "mappingbased_properties_cleaned_en.nt");

    public static final File dbPediaExtractedExpansionTermsFile = new File(dbPediaDirProject, "extractedExpansion.bin");
}
