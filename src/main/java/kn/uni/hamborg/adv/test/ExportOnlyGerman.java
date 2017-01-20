/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.data.emm.EMMChannel;
import kn.uni.hamborg.data.emm.EMMItem;
import kn.uni.hamborg.data.emm.EMMRssParser;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ExportOnlyGerman {

    private static final Logger LOG = Logger.getLogger(ExportOnlyGerman.class.getSimpleName());

    public static void main(String[] args) throws IOException {
        File[] filesInDir = EMMRssParser.getFilesForDirectory(LuceneConfig.DATA_DIR_2014);
        BufferedWriter bw = new BufferedWriter(new FileWriter("onlygerman.txt"));
        for (File file : filesInDir) {
            try {
                EMMChannel channel = EMMRssParser.getChannelForFile(file);
                if (channel.getLanguage().toLowerCase().equals("de")) {
                    for (EMMItem item : channel.getItems()) {
                        bw.append(item.getTitle());
                        bw.append(" ");
                        bw.append(item.getDescription());
                        bw.append(" ");
                        bw.append(item.getText().getValue());
                        bw.append(System.lineSeparator());
                    }
                    bw.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bw.close();
    }
}
