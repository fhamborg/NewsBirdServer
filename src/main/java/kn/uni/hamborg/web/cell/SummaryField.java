/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web.cell;

import kn.uni.hamborg.data.light.LightDoc;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public enum SummaryField {

    TITLE(LightDoc.TITLE), TITLE_STEMMED(LightDoc.TITLE_STEMMED),
    CONTENT(LightDoc.CONTENT), CONTENT_STEMMED(LightDoc.CONTENT_STEMMED),
    DESCRIPTION(LightDoc.DESCRIPTION), DESCRIPTION_STEMMED(LightDoc.DESCRIPTION_STEMMED),;

    private final String fieldname;

    private SummaryField(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getFieldname() {
        return fieldname;
    }

    public static SummaryField fromLightDocField(String fieldname) {
        for (SummaryField sf : values()) {
            if (sf.fieldname.equals(fieldname)) {
                return sf;
            }
        }
        throw new RuntimeException("fieldname " + fieldname + " does not exist");
    }

    /**
     * returns in preferred order for summarization field
     *
     * @return
     */
    public static String[] getForSummarizationCalculationInPreferredOrder() {
        return new String[]{
            CONTENT_STEMMED.getFieldname(), TITLE_STEMMED.getFieldname(), DESCRIPTION_STEMMED.getFieldname()
        };
    }
}
