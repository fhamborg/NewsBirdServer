/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web;

import java.util.logging.Logger;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ClientSideOptions {

    private static final Logger LOG = Logger.getLogger(ClientSideOptions.class.getSimpleName());

    private static final String[] BOOLEAN_DEFAULT_TRUE = new String[]{"true", "false"};
    private static final String[] BOOLEAN_DEFAULT_FALSE = new String[]{"false", "true"};

    private final String[] cellBackgroundColor = new String[]{"topicId", "cellImportanceMeasurement"};
    private final String[] cellImportanceMeasurement = new String[]{"topicQuery-cellQuery-ratio", "maxTopicFrequencyInCell", "sentenceProbability"};
    private final String[] cellBackgroundImage = BOOLEAN_DEFAULT_FALSE;
    private final String[] sortMatrixBy = new String[]{"fullRowsAndCols"};

    private final MetaOptions metaOptions = new MetaOptions();

    private static class MetaOptions {

        private final String[] dropdownNames = new String[]{
            // client side options
            "cellImportanceMeasurement",
            "cellBackgroundColor",
            "cellBackgroundImage"

        };

        private final String[] textinputNames = new String[]{};

        private final String[] buttonNames = new String[]{
            "sortMatrixBy"
        };
    }
}
