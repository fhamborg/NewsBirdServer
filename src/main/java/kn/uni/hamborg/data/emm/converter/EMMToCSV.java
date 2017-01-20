package kn.uni.hamborg.data.emm.converter;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.data.emm.EMMChannel;
import kn.uni.hamborg.data.emm.EMMItem;
import kn.uni.hamborg.data.emm.EMMRssParser;
import kn.uni.hamborg.language.Language;

/**
 *
 * @author felix
 */
public class EMMToCSV {

    public static void contentToCsv(final Writer writer) throws IOException {
        final CSVWriter csv = new CSVWriter(writer);
        int docs = 0;

        EMMChannel[] channels = EMMRssParser.getChannelsForDirectory(new File("./rawdata/"));
        for (EMMChannel channel : channels) {
            //EMMChannel channel = EMMRssParser.getChannelForFile(EMMRssParser.DEBUG_SAMPLE_FILE);
            for (EMMItem item : channel.getItems()) {
                if (item.getLanguage().equals(Language.EN.toString())) {
                    String content = preprocess(item.getText().getValue());
                    if (isPutin(content)) {
                        csv.writeNext(new String[]{item.getId(), content});
                        docs++;
                    }
                }
            }
        }
        System.out.println("written " + docs + " " + Language.EN.toString() + " items");

        csv.close();
    }

    public static String preprocess(String s) {
        return s.replaceAll("\"", "").replaceAll("\n", " ").toLowerCase();
    }

    public static boolean isPutin(String s) {
        return s.contains("putin");
    }

    public static boolean isRusslandOrFinanz(String s) {
        return s.contains("russland") || s.contains("russen") || s.contains("finanz");
    }

    public static void main(String[] args) {
        try {
            contentToCsv(new FileWriter(new File("knime-pca-out/putin.csv")));
        } catch (IOException ex) {
            Logger.getLogger(EMMToCSV.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
