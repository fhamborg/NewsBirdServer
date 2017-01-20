/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.translator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.config.OtherConfig;
import kn.uni.hamborg.data.emm.EMMChannel;
import kn.uni.hamborg.data.emm.EMMItem;
import kn.uni.hamborg.data.emm.EMMRssParser;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.data.light.LightDocConverter;
import static kn.uni.hamborg.language.translator.LightDocTranslator.getIdForDocAndField;
import kn.uni.hamborg.utils.DateTimeUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * This can filter a given dataset for a date and translate all EMMItems within
 * that are not in English, into English. Progress is saved, so that it can be
 * resumed.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EMMDataSetTranslator {

    private static final Logger LOG = Logger.getLogger(EMMDataSetTranslator.class.getSimpleName());

    private static final DateTime startdate = new DateTime(2014, 11, 6, 0, 0, DateTimeZone.UTC).withTimeAtStartOfDay();
    private static final DateTime enddate = DateTimeUtils.getEndOfDay(new DateTime(2014, 11, 9, 0, 0, DateTimeZone.UTC));
    private static final long startlong = startdate.getMillis();
    private static final long endlong = enddate.getMillis();

    private static final LightDocConverter LIGHT_DOC_CONVERTER = new LightDocConverter();

    private static long getLongFromFilename(String filename) {
        String longstrong = filename.substring(9, 22);
        long time = Long.valueOf(longstrong);
        return time;
    }

    private static boolean isInBetweenMinMaxDate(long time) {
        return startlong <= time && time <= endlong;
    }

    public static LightDoc translate(LightDoc doc) {
        if (doc == null) {
            LOG.severe("doc NULL");
            return null;
        }
        if (doc.getContent() == null) {
            LOG.severe("content or title NULL");
            return null;
        }
        if (doc.getTitle() == null) {
            LOG.severe("title NULL");
            return null;
        }
        if (doc.getDescription() == null) {
            LOG.severe("description NULL");
            return null;
        }

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

    private File getFileForChannel(EMMChannel channel) {
        // return new File(LuceneConfig.DATA_DIR_14_11_06_TO_08_TRANSLATED, channel.getFilename());
        return null;
    }

    private void run() {
        // check whether we have any previous files

        BufferedWriter progresswriter = null;
        try {
            final List<String> filestrings = Files.readAllLines(OtherConfig.translationProgress.toPath());
            //   LuceneConfig.DATA_DIR_14_11_06_TO_08_TRANSLATED.mkdirs();
            LOG.log(Level.INFO, "creating list of all files in {0} ...", LuceneConfig.DATA_DIR_2014);
            final File[] files = EMMRssParser.getFilesForDirectory(LuceneConfig.DATA_DIR_2014);
            LOG.log(Level.INFO, "found {0} files", files.length);

            progresswriter = new BufferedWriter(new FileWriter(OtherConfig.translationProgress));

            long charCount = 0;
            int count = 0;
            for (File file : files) {
                if (filestrings.contains(file.getPath())) {
                    LOG.log(Level.INFO, "skipping {0} as it was already processed", file.getPath());
                    count++;
                    continue;
                }
                // check date by filename
                if (!isInBetweenMinMaxDate(getLongFromFilename(file.getName()))) {
                    //LOG.log(Level.INFO, "{0}" + "skipping " + " because of wrong date", file.getPath());
                    count++;
                    continue;
                }

                try {
                    final EMMChannel channel = EMMRssParser.getChannelForFile(file);
                    BufferedWriter bw = new BufferedWriter(new FileWriter(getFileForChannel(channel)));

                    // check date
                    /*int dayofmonth = channel.getPubDate().getDayOfMonth();
                     int monthofyear = channel.getPubDate().getMonthOfYear();
                     int year = channel.getPubDate().getYear();
                     if (dayofmonth != 7 || monthofyear != 11 || year != 2014) {
                     LOG.log(Level.INFO, "skipping {0} because of wrong date", file.getPath());
                     count++;
                     continue;
                     } else {
                     System.out.println("" + channel.getPubDate());
                     }*/
                    System.out.println(channel.getPubDate());

                    // will always be lowercase,
                    List<LightDoc> englishDocs = new ArrayList<>();

                    for (EMMItem item : channel.getItems()) {
                        // die sprache des items ist immer gleich des channels
                        LightDoc lightDoc = LIGHT_DOC_CONVERTER.convert(channel, item);

                        if (!channel.getLanguage().equals("en")) {
                            bw.append(lightDoc.getContent());
                            bw.append(System.lineSeparator());
                            bw.append(lightDoc.getTitle());
                            charCount += lightDoc.getContent().length() + lightDoc.getTitle().length();
                        }

                        if (lightDoc != null) {
                            //System.out.println(lightDoc.toString());
                            englishDocs.add(lightDoc);

                        } else {

                        }

                    }

                    // ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getFileForChannel(channel)));
                    // oos.writeObject(englishDocs);
                    // oos.close();
                    LOG.log(Level.INFO, "wrote {0} items to {1}", new Object[]{channel.getItems().size(), getFileForChannel(channel)});
                    bw.close();
                    progresswriter.append(channel.getPathname());
                    progresswriter.newLine();
                    progresswriter.flush();

                    count++;
                    if (count % 1000 == 0) {
                        LOG.log(Level.INFO, "{0} / {1}", new Object[]{count, files.length});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            System.out.println("wrote unicode = " + charCount);
        } catch (IOException ex) {
            Logger.getLogger(EMMDataSetTranslator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                progresswriter.close();
            } catch (IOException ex) {
                Logger.getLogger(EMMDataSetTranslator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        EMMDataSetTranslator ct = new EMMDataSetTranslator();
        ct.run();
    }

}
