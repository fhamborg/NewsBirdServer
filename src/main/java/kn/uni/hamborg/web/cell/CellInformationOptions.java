/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web.cell;

import java.util.logging.Logger;

/**
 * Represents possible options for displaying information within a cell.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CellInformationOptions {

    private static final Logger LOG = Logger.getLogger(CellInformationOptions.class.getSimpleName());
    private static final String[] BOOLEAN_DEFAULT_TRUE = new String[]{"true", "false"};
    private static final String[] BOOLEAN_DEFAULT_FALSE = new String[]{"false", "true"};

    private final SummaryElement[] summaryElementOptions = SummaryElement.values();
    private final TokenWeighting[] tokenWeightingOptions = TokenWeighting.values();
    private final ElementOrder[] elementOrderOptions = ElementOrder.values();
    private final SummaryField[] summaryFieldOptions = SummaryField.values();

    private final int minTokensInSentence = 4;
    private final int numberOfSentences = 1;

    // Sentence based
    // Lin2002Single
    private final String[] summarizationSentence_Lin2002Single_StartingWithStigmaWords_ReducedScore = BOOLEAN_DEFAULT_TRUE;
    // Felix Scorer
    private final String[] summarizationSentence_FelixScorer = BOOLEAN_DEFAULT_TRUE;

    // other
    private final String[] summarizationSentence_TopChunksOnly = BOOLEAN_DEFAULT_FALSE;

    // Token based
    // other
    private final String[] summarizationToken_HideSynonyms = BOOLEAN_DEFAULT_TRUE;
    private final String[] summarizationToken_HideStopWords = BOOLEAN_DEFAULT_TRUE;

    private final MetaOptions metaOptions = new MetaOptions();

    private static class MetaOptions {

        private final String[] dropdownNames = new String[]{
            // client side options
            "visOptions_CellImportanceMeasurement",
            // server side options
            "summaryFieldOptions",
            "summaryElementOptions",
            "tokenWeightingOptions",
            "elementOrderOptions",
            "summarizationSentence_Lin2002Single_StartingWithStigmaWords_ReducedScore",
            "summarizationSentence_FelixScorer",
            "summarizationSentence_TopChunksOnly",
            //"summarizationToken_HideSynonyms"
            "summarizationToken_HideStopWords"
        };

        private final String[] textinputNames = new String[]{
            "minTokensInSentence", "numberOfSentences"};

        private final String[] buttonNames = new String[]{};
    }
}
