/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import kn.uni.hamborg.web.cell.SummaryField;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class Summaries {

    private static final Logger LOG = Logger.getLogger(Summaries.class.getSimpleName());

    public static final String NOT_DEFINED = "not defined";

    private final Map<SummaryField, Summary> summaries = new HashMap<>();
    private final List<String> topDocumentIds = new ArrayList<>();

    public void addSummary(SummaryField id, Summary summary) {
        summaries.put(id, summary);
    }

    public Map<SummaryField, Summary> getSummaries() {
        return summaries;
    }

    public void addTopSummarizationDocumentId(String id) {
        topDocumentIds.add(id);
    }

    public List<String> getTopDocumentIds() {
        return topDocumentIds;
    }

    public static Summaries createEmptySummaries() {
        Summaries summaries = new Summaries();
        for (SummaryField fieldname : SummaryField.values()) {
            final Summary summary = new Summary(new StringScore[]{new StringScore(NOT_DEFINED, 1.0)},
                    new StringScore[]{new StringScore(NOT_DEFINED, 1.0)},
                    new StringScore[]{new StringScore(NOT_DEFINED, 1.0)});
            summaries.addSummary(fieldname, summary);
        }
        return summaries;
    }

}
