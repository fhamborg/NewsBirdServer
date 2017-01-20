/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.data.light;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import kn.uni.hamborg.data.emm.EMMChannel;
import kn.uni.hamborg.data.emm.EMMItem;
import org.joda.time.DateTime;

/**
 * This class converts an {@link EMMChannel} into corresponding {@link LightDoc}
 * instances. Thereby we perform preprocessing:<br>
 * <ul>
 * <li>
 * If {@link EMMItem#textEn} is not {@code null} we use that (assuming that
 * {@link EMMItem#text} is in a foreign language.</li>
 * </ul>
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class LightDocConverter {

    public int counterNonEnglishArticlesWithoutTranslation = 0;
    public int counterNonEnglishArticlesWithTranslation = 0;
    public int counterEnglishArticles = 0;
    public int counterWithoutText = 0;

    // private final boolean translateToEnglish;
    public LightDocConverter() {
        //   this.translateToEnglish = translateToEnglish;
    }

    /**
     * Looks like the quality of the automatically translated at EMM is not so
     * good, so either it already is in English (in title) or we get the
     * translation ourselves, but never use the texten.
     *
     * @param item
     * @return
     */
    private static String getText(EMMItem item) {
        String text = null;
        // text-en
       /* try {
         text = item.getTextEn().getValue();
         if (text != null && text.length() > 0) {
         return text;
         }
         } catch (NullPointerException e) {
         }
         // description-en
         */
        // text
        text = item.getText().getValue();

        // description
        return text;
    }

    /**
     * Looks like the quality of the automatically translated at EMM is not so
     * good, so either it already is in English (in title) or we get the
     * translation ourselves, but never use the titleen.
     *
     * @param item
     * @return
     */
    private static String getTitle(EMMItem item) {
        /* return item.getTitleEn() != null
         ? item.getTitleEn()
         : item.getTitle();*/
        return item.getTitle();
    }

    /**
     * Converts an EMMItem in the context of its EMMChannel to a LightDoc (which
     * in turn can be used for Lucene).
     *
     * @param channel
     * @param item
     * @return
     */
    public static LightDoc convert(EMMChannel channel, EMMItem item) {
        final String id = item.getId();
        final String text = getText(item);
        final String title = getTitle(item);
        final String description = item.getDescription();

        String channelGuid = channel.getGuid();

        final String channelLanguage = channel.getLanguage().toLowerCase();

        String link = item.getLink();

        DateTime pubDate = item.getPubDate();

        String pubLand = item.getSource().getCountry();

        String channelPath = channel.getPathname();

        int positionInChannel = item.getPositionInChannel();

        return new LightDoc(id, text, title, description, channelGuid, channelLanguage, link,
                pubDate, pubLand, channelPath, positionInChannel);
    }

    /**
     * Convert an {@link EMMChannel}'s items to {@link LightDoc} instances.
     *
     * @param channel
     * @return
     */
    public static LightDoc[] convert(EMMChannel channel) {
        List<LightDoc> docs = new ArrayList<>();
        for (EMMItem item : channel.getItems()) {
            LightDoc ld = convert(channel, item);
            if (ld != null) {
                docs.add(ld);
            }
        }

        return docs.toArray(new LightDoc[docs.size()]);
    }

    /**
     * Convert multiple {@link EMMChannel} instances to {@link LightDoc}
     * instances.
     *
     * @param channels
     * @return
     */
    public static LightDoc[] convert(EMMChannel[] channels) {
        List<LightDoc> docs = new ArrayList<>();
        for (EMMChannel c : channels) {
            docs.addAll(Arrays.asList(convert(c)));
        }

        return docs.toArray(new LightDoc[docs.size()]);
    }

    public static void main(String[] args) {

    }

}
