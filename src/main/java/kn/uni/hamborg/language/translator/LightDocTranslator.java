/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.translator;

import java.util.logging.Logger;
import kn.uni.hamborg.data.light.LightDoc;

/**
 * Translates a LightDoc into English language by usage of
 * {@link EasyTranslator}.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class LightDocTranslator {

    private static final Logger LOG = Logger.getLogger(LightDocTranslator.class.getSimpleName());

    /**
     * Translates the given LightDoc non-English to English language. Thereby
     * CONTENT, TITLE, DESCRIPTION are translated.
     *
     * @param doc
     * @return
     */
    public static LightDoc translateToEnglish(LightDoc doc) {
        final String content = EasyTranslator.translateToEnglish(getIdForDocAndField(doc, LightDoc.CONTENT), doc.getContent());
        final String title = EasyTranslator.translateToEnglish(getIdForDocAndField(doc, LightDoc.TITLE), doc.getTitle());
        final String description = EasyTranslator.translateToEnglish(getIdForDocAndField(doc, LightDoc.DESCRIPTION), doc.getDescription());

        return new LightDoc(
                doc.getId(),
                content,
                title,
                description,
                doc.getChannelGuid(),
                doc.getChannelLanguage(),
                doc.getLink(),
                doc.getPubDate(),
                doc.getPubLand(),
                doc.getChannelFile(),
                doc.getPositionInChannel()
        );
    }

    public static String getIdForDocAndField(LightDoc doc, String field) {
        return doc.getId() + "_" + field;
    }
}
