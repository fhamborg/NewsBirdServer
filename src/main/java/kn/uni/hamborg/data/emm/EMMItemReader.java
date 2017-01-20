package kn.uni.hamborg.data.emm;

import java.io.File;
import kn.uni.hamborg.data.light.LightDoc;
import org.apache.lucene.document.Document;

/**
 *
 * @author Felix Hamborg
 */
public class EMMItemReader {

    /**
     * Reads a file on disk and returns the {@link EMMItem} at the
     * {@code positionInChannel}-th position with the {@link EMMChannel} within
     * the file.
     *
     * @param channelFile
     * @param positionInChannel
     * @return
     */
    public static EMMItem readEMMItem(final File channelFile, final int positionInChannel) {
        return EMMRssParser.getChannelForFile(channelFile).getItems().get(positionInChannel);
    }

    /**
     * Extracts file path and position in channel from the given document. The
     * corresponding file is opened and the EMMItem at the position returned.
     *
     * @param doc
     * @return
     */
    public static EMMItem readEMMItem(final Document doc) {
        return readEMMItem(new File(doc.get(LightDoc.CHANNEL_FILE)),
                new Integer(doc.get(LightDoc.CHANNEL_POSITION)));
    }

    
    public static String filename = "/home/felix/courses/mp_local/04-nov-2014/incoming-1415265441407-dailytimesPK.xml";//"/home/felix/courses/mp_local/04-nov-2014/incoming-1415369628995-theatlantic.xml";
    public static int pos = 23;//0;

    public static void main(String[] args) {
        File f = new File(filename);

        System.out.println(readEMMItem(f, pos).getTitle());
        System.out.println(readEMMItem(f, pos).getText());
    }
}
