/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import kn.uni.hamborg.data.light.LightDoc;
import org.apache.lucene.document.Document;
import org.joda.time.DateTime;

/**
 * Helper class to read certain fields from a Lucene document and to convert it
 * back to a LightDoc
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class LightDocUtils {

    /**
     * Extracts the DateTime from the Lucene Document doc
     *
     * @param doc
     * @return
     */
    public static DateTime getPubDate(Document doc) {
        return new DateTime(Long.valueOf(doc.get(LightDoc.PUB_DATE)));
    }

    public static String getContent(Document doc) {
        return doc.get(LightDoc.CONTENT);
    }

    public static String getCountryCode(Document doc) {
        return doc.get(LightDoc.PUB_COUNTRY);
    }

    public static String getId(Document doc) {
        return doc.get(LightDoc.ID);
    }

    public static String getTitle(Document doc) {
        return doc.get(LightDoc.TITLE);
    }

    public static String getPubGuid(Document doc) {
        return doc.get(LightDoc.CHANNEL_GUID);
    }

    public static String getDescription(Document doc) {
        return doc.get(LightDoc.DESCRIPTION);
    }

    public static String getPubUrl(Document doc) {
        return doc.get(LightDoc.PUB_URL);
    }

    public static String getChannelGuid(Document doc) {
        return doc.get(LightDoc.CHANNEL_GUID);
    }
}
