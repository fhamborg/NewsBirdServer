/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.data.personal;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.config.SocialConfig;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.lucene.index.GeneralLightDocIndexer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Parses the messages.htm by Facebook. You can get it by going here and request
 * a download of all personal information:
 * https://www.facebook.com/help/131112897028467/
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class FacebookMessagesToLightDocs {

    private static final Logger LOG = Logger.getLogger(FacebookMessagesToLightDocs.class.getSimpleName());

    public void parseHtmlFacebookMessageFile() throws IOException {
        BufferedReader bw = new BufferedReader(new InputStreamReader(new FileInputStream(SocialConfig.facebookMessageHistoryHtmlFile),
                "UTF-8"));

        List<LightDoc> docs = new ArrayList<>();

        String content;
        final String title = "notitle";
        String channelGuid = null;
        final String channelLanguage = "EN";
        final int positionInChannel = 1337;
        final String channelFile = "notreal";
        final String link = "http://not.real";
        DateTime pubDate = null;
        String pubLand = null;

        List<String> recipients = null;

        final String threadTag = "<div class=\"thread\">";
        final int threadTagLen = threadTag.length();
        final String closeSpan = "</span>";
        final String userTag = "<span class=\"user\">";
        final int userTagLen = userTag.length();
        final String dateTag = "<span class=\"meta\">";
        final int dateTagLen = dateTag.length();
        // see http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
        final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("EEEE, MMMM d, Y 'at' h:ma z");

        int idCount = 0;
        String line = null;
        while ((line = bw.readLine()) != null) {
            //System.out.println(line);
            if (line.contains(threadTag)) {
                // here we can get the recipients
                recipients = new ArrayList<>();
                for (String recipient : line.substring(line.indexOf(threadTag) + threadTagLen).split(",")) {
                    recipient = recipient.trim();
                    recipients.add(recipient);

                }

            } else if (line.contains(userTag)) {
                // <span class="user">Felix Fox</span>
                line = line.substring(line.indexOf(userTag) + userTagLen, line.indexOf(closeSpan)).trim();
                channelGuid = line;
                pubLand = line;
            } else if (line.contains(dateTag)) {
                // <span class="meta">Saturday, August 11, 2012 at 12:59pm PDT</span>
                line = line.substring(line.indexOf(dateTag) + dateTagLen, line.indexOf(closeSpan));
                pubDate = dateTimeFormatter.parseDateTime(line);

            } else if (line.contains("<p>")) {
                int start = line.indexOf("<p>");
                start += 3;

                int end = line.indexOf("</p>");
                String tmp = "";

                if (end != -1) {
                    tmp += line.substring(start, end);
                } else {
                    // multiline
                    tmp += line.substring(start);

                    while ((line = bw.readLine()) != null) {
                        line = line.trim();

                        end = line.indexOf("</p>");
                        if (end != -1) {
                            // found end, finish
                            tmp += " " + line.substring(0, end);
                            break;
                        } else {
                            tmp += line;
                        }
                    }
                }
                content = tmp;

                // at this point the paragraph is always finished and we can create a new lightdoc
                LightDoc ld = new LightDoc(String.valueOf(idCount), content, title, title, channelGuid,
                        channelLanguage, link, pubDate, pubLand, recipients.toArray(new String[0]),
                        channelFile, positionInChannel);

                System.out.println(ld.toString());
                docs.add(ld);

                idCount++;

                // reset everything
                content = null;
                channelGuid = null;
                pubDate = null;
                pubLand = null;

                // do not reset recipients because they are valid until we see new
                // recipients = null;
            }

        }

        bw.close();

        LOG.log(Level.INFO, "finished reading all messages {0}", idCount);

        LOG.info("writing to Index");
        GeneralLightDocIndexer.createIndexWithLightDocs(LuceneConfig.INDEX_DIR_FACEBOOK_MESSAGE_HISTORY, docs);
    }

    public static void main(String[] args) throws IOException {
        new FacebookMessagesToLightDocs().parseHtmlFacebookMessageFile();

    }
}
