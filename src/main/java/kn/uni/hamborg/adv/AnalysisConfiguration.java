/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv;

import java.util.logging.Logger;
import kn.uni.hamborg.data.light.LightDoc;

/**
 * This class represents a configuration for the analysis process. It is
 * actually a quick hack to allow any component, currently only Web-controllers,
 * to change/set these parameters, and any other component, e.g., the
 * Summarizer, to access such values this way.
 *
 * Actually some of these parameters are not changed by the users at all, but
 * only set in here (set by the developer). Convention: All of them are
 * {@code final}.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class AnalysisConfiguration {

    private static final Logger LOG = Logger.getLogger(AnalysisConfiguration.class.getSimpleName());

    public static final boolean onlyTopTopicUsedForSummarizationQuery = false;
    public static final double summarization_OrderSentencesByFirstOccurenceInDoc_PositionScoreFactor = 10000.0;
    public static final boolean topicQueryHasTopTermsSuperBoosted = true;
    public static final boolean topicQueryForceAllTopTermsIncluded = false;
    public static final boolean enabledCellNgramScorer = false;

    // user set variables
    public static boolean summarization_OrderSentencesByFirstOccurenceInDoc = true;

    // from lin2002single
    public static boolean summarization_Lin2002Single_FirstSentencesOnly = false;
    public static boolean summarization_Lin2002Single_StartingWithStigmaWords_ReducedScore = false;
    // not implemented yet
    public static boolean summarization_Lin2002Single_Overlap = false;

    public static final String topicMergingSummarizationField = LightDoc.TITLE_STEMMED;

    // which field should be used to compute topics (before summarization scoring)
    public static String topicField;

}
