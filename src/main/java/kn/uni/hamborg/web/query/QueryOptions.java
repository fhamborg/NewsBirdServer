/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web.query;

import java.util.logging.Logger;
import kn.uni.hamborg.adv.table.FilterDimension;
import kn.uni.hamborg.adv.topic.MalletTopicExtractor;
import kn.uni.hamborg.utils.EnumUtils;
import kn.uni.hamborg.web.cell.SummaryField;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class QueryOptions {

    private static final Logger LOG = Logger.getLogger(QueryOptions.class.getSimpleName());

    private final String[] filterDimensionClasses;
    private final String[] topicField = SummaryField.getForSummarizationCalculationInPreferredOrder();
    private final String[] topicCellDocumentMergeType = EnumUtils.getNames(MalletTopicExtractor.CellDocumentMergeType.class);

    public QueryOptions() {
        filterDimensionClasses = new String[FilterDimension.FilterDimensionClasses.length];
        for (int i = 0; i < filterDimensionClasses.length; i++) {
            filterDimensionClasses[i] = FilterDimension.FilterDimensionClasses[i].getSimpleName();
        }
    }

    private final int numberOfTopicsPerCell = 1;
    // private final String[] onlyTopTopicForSummarizationQuery = new String[]{"true", "false"};
    private final String[] summarization_OrderSentencesByFirstOccurenceInDoc = new String[]{"false", "true"};

    // Lin2002Single
    private final String[] summarization_Lin2002Single_FirstSentencesOnly = new String[]{"true", "false"};
    private final String userFilterFewRequiredTerms = "";
    private final String userFilterAdditionalTerms = "";

    private final MetaOptions metaOptions = new MetaOptions();

    private static class MetaOptions {

        private final String[] dropdownNames = new String[]{ //     "onlyTopTopicForSummarizationQuery"
            "summarization_OrderSentencesByFirstOccurenceInDoc", "summarization_Lin2002Single_FirstSentencesOnly",
            "topicCellDocumentMergeType", "topicField"
        };

        private final String[] textinputNames = new String[]{
            "numberOfTopicsPerCell", "userFilterFewRequiredTerms", "userFilterAdditionalTerms"
        };

        private final String[] buttonNames = new String[]{};
    }
}
