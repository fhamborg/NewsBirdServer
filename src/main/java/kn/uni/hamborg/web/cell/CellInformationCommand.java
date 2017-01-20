/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web.cell;

import com.google.gson.annotations.SerializedName;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.table.FilterCell;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This represents cell information options that have been sent by the client.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CellInformationCommand {

    private static final Logger LOG = Logger.getLogger(CellInformationCommand.class.getSimpleName());

    @SerializedName("summaryElementOptions")
    private SummaryElement summaryElement;
    @SerializedName("tokenWeightingOptions")
    private TokenWeighting tokenWeighting;
    @SerializedName("elementOrderOptions")
    private ElementOrder elementOrder;
    @SerializedName("summaryFieldOptions")
    private SummaryField summaryField;
    @SerializedName("minTokensInSentence")
    private int minTokensInSentence;
    @SerializedName("summarizationSentence_Lin2002Single_StartingWithStigmaWords_ReducedScore")
    private boolean summarizationSentence_Lin2002Single_StartingWithStigmaWords_ReducedScore;
    @SerializedName("summarizationSentence_FelixScorer")
    private boolean summarizationSentence_FelixScorer;
    @SerializedName("summarizationSentence_TopChunksOnly")
    private boolean summarizationSentence_TopChunksOnly;
    @SerializedName("numberOfSentences")
    private int numberOfSentences;
    @SerializedName("summarizationToken_HideSynonyms")
    private boolean summarizationToken_HideSynonyms;
    @SerializedName("summarizationToken_HideStopWords")
    private boolean summarizationToken_HideStopWords;

    public boolean isSummarizationSentence_FelixScorer() {
        return summarizationSentence_FelixScorer;
    }

    public boolean isSummarizationToken_HideStopWords() {
        return summarizationToken_HideStopWords;
    }

    public boolean isSummarizationToken_HideSynonyms() {
        return summarizationToken_HideSynonyms;
    }

    public int getNumberOfSentences() {
        return numberOfSentences;
    }

    public boolean isSummarizationSentence_TopChunksOnly() {
        return summarizationSentence_TopChunksOnly;
    }

    public SummaryElement getSummaryElement() {
        return summaryElement;
    }

    public TokenWeighting getTokenWeighting() {
        return tokenWeighting;
    }

    public SummaryField getSummaryField() {
        return summaryField;
    }

    public ElementOrder getElementOrder() {
        return elementOrder;
    }

    public int getMinTokensInSentence() {
        return minTokensInSentence;
    }

    public boolean isSummarizationSentence_Lin2002Single_StartingWithStigmaWords_ReducedScore() {
        return summarizationSentence_Lin2002Single_StartingWithStigmaWords_ReducedScore;
    }

    /**
     * This returns a key String which can be used for accessing the
     * corresponding attribute in the {@link FilterCell} based on this
     * instance's option configuration.
     *
     * @return
     */
    public String getAttributeKey() {
        return "cellinfo_"
                + getSummaryElement() + "_"
                + getTokenWeighting() + "_"
                + getElementOrder() + "_"
                + getSummaryField() + "_"
                + getMinTokensInSentence() + "_"
                + isSummarizationSentence_Lin2002Single_StartingWithStigmaWords_ReducedScore() + "_"
                + isSummarizationSentence_FelixScorer() + "_"
                + isSummarizationSentence_TopChunksOnly() + "_"
                + getNumberOfSentences() + "_"
                + isSummarizationToken_HideStopWords() + "_"
                + isSummarizationToken_HideSynonyms() + "_";
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
