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
public class StatConfig {

    private static final Logger LOG = Logger.getLogger(StatConfig.class.getSimpleName());

    private static final String statResults = "statResults/";
    private static final String statModels = "models/";

    private static final String subjectivity = "subjectivity";

    private static final File basePathResults = new File(FileConfig.basePathMpProject, statResults);
    private static final File basePathModels = new File(FileConfig.basePathMpProject, statModels);

    public static final File pathSubjectivityResults = new File(basePathResults, subjectivity);
    public static final File pathSubjectivityModel = new File(basePathModels, subjectivity);

    public static final File pathSubjectivityModelCountry = new File(pathSubjectivityModel, "country.bin");
    public static final File pathSubjectivityModelPublisher = new File(pathSubjectivityModel, "publisher.bin");
    public static final File pathSubjectivityModelDayDate = new File(pathSubjectivityModel, "daydate.bin");

}
