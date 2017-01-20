/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.translator;

import com.google.common.base.Splitter;
import com.memetix.mst.translate.Translate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.utils.DateTimeUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EasyTranslator {

    private static final Logger LOG = Logger.getLogger(EasyTranslator.class.getSimpleName());

    private static final ITranslationStorage TRANSLATION_STORAGE = TranslationStorageDB.getInstance();

    static public long textTooLargeCount = 0;

    static {
        //Translate.setClientId("emmtranslatems");
        //Translate.setClientSecret("Q7dcbvVRtgwX3YC32V+GxF08b8y8ZPWuHfI8+42s9u4=");
        // Translate.setClientId("fickfistfist");
        // Translate.setClientSecret("Cu7GCHQ/iz9yoZxCnHdp+8wf8LQ0gIfgPXd2RXa37w8=");
        // Translate.setClientId("emmshid");
        // Translate.setClientSecret("3i1T+plhE8XiH4fjlbh9WIsD9PcjpHNhaOB5CRKspBE=");
        //Translate.setClientId("pisdapisda");
        //Translate.setClientSecret("PBwrD6CDpgAOsX8nCBP39t91tPLx2+cuDQ4bTV1JgrI=");
        Translate.setClientId("emmtranslatorfh");
        Translate.setClientSecret("RDjr0vVNdbTiKcTPgxizJTwkaC2olOfSshks2njAxds=");
    }
    private static final int DISABLE_CHECK_AFTER_COUNT = Integer.MAX_VALUE;
    private static int lastKnown = 0;

    /**
     * Translates the given text into English. The text is also stored in the
     * local translation storage. Before sending it to the translation server,
     * it is checked whether there is already a translation for the given id. If
     * true, that translation is returned instead of sending the translation
     * request to the server.
     *
     * @param id
     * @param text
     * @return
     */
    public static String translateToEnglish(final String id, final String text) {
        if (lastKnown < DISABLE_CHECK_AFTER_COUNT) {
            String oldTranslation = TRANSLATION_STORAGE.getTranslation(id);

            if (oldTranslation != null) {
                lastKnown = 0;
                //System.out.println("using old translation " + id + ": " + oldTranslation);
                return oldTranslation;
            } else {
                lastKnown++;
            }
        } else {
            //System.out.println("no check (" + lastKnown + ")");
        }

        // split into multiple parts if too long
        StringBuilder sb = new StringBuilder();
        //byte[] bytes = text.getBytes(Charset.forName("UTF-8"));
        //for (int i = 0; i < bytes.length % 5000; i++) {
        //    byte[] subbytes = Arrays.copyOfRange(bytes, i * 5000, Math.min(bytes.length, (i + 1) * 5000));
        //    String text1 = new String(subbytes, Charset.forName("UTF-8"));

        Iterable<String> texts = Splitter.fixedLength(5000).split(text);
        for (String text1 : texts) {
            try {
                String translated = Translate.execute(text1, com.memetix.mst.language.Language.ENGLISH);
                sb.append(translated);

                if (translated.startsWith("TranslateApiException")) {
                    throw new RuntimeException("out of balance");
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                if (ex.getMessage().equals("out of balance")) {
                    throw new RuntimeException(ex);
                } else if (ex.getMessage().startsWith("TEXT_TOO_LARGE")) {
                    System.out.println("TEXT_TOO_LARGE = " + textTooLargeCount);
                    textTooLargeCount++;
                }
                Logger.getLogger(EasyTranslator.class.getName()).log(Level.SEVERE, null, ex);
                LOG.log(Level.INFO, "length = {0}", text1.length());
                LOG.log(Level.INFO, "input text = {0}", text1);
                return null;
            }
        }

        String translation = sb.toString();
        TRANSLATION_STORAGE.putTranslation(id, translation);

        //System.out.println("'" + text + "'");
        System.out.println("--> '" + translation + "'");

        return translation;
    }

    public static void main(String[] args) {
        System.out.println(translateToEnglish("1", "hallo felix! ich bin es, dieter! wie geht es dir denn eigentlich heute so?"));
        System.out.println(translateToEnglish("1", "If this continues, Saxony-Anhalt is soon really only have a nursing home,\" Eichler said the MZ."));
        System.out.println(translateToEnglish("1", "hallo felix! ich bin es, dieter!"));

        System.out.println(new Date(1405971995615L).toString());
        DateTime startdate = new DateTime(2014, 11, 7, 0, 0, DateTimeZone.UTC).withTimeAtStartOfDay();
        DateTime enddate = DateTimeUtils.getEndOfDay(startdate);
        long startlong = startdate.getMillis();
        long endlong = enddate.getMillis();
        System.out.println(startlong);
        System.out.println(endlong);
    }
}
