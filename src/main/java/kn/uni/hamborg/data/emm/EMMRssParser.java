/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.data.emm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EMMRssParser {

    public static final File DEBUG_SAMPLE_FILE = new File("rawdata/incoming-1403028915124-usnews.xml");

    public static void main(String[] args) {
        EMMRssParser.getChannelsForDirectory(new File("rawdata/"));
    }

    public static class DateAdapterItem extends XmlAdapter<String, DateTime> {

        private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mmZ", Locale.US);

        @Override
        public String marshal(DateTime v) throws Exception {
            return dateFormat.format(v);
        }

        @Override
        public DateTime unmarshal(String v) throws Exception {
            return new DateTime(dateFormat.parse(v));
        }
    }

    public static class DateAdapterChannel extends XmlAdapter<String, DateTime> {

        private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

        @Override
        public String marshal(DateTime v) throws Exception {
            return dateFormat.format(v);
        }

        @Override
        public DateTime unmarshal(String v) throws Exception {
            return new DateTime(dateFormat.parse(v));
        }
    }

    /**
     * Iterates over all *.xml files and tries to convert them in EMMRss
     * documents. Afterwards, from each of those its EMMChannel is retrieved,
     * put into a list and this list is returned.
     *
     * @param directoryPath
     * @return
     */
    public static EMMChannel[] getChannelsForDirectory(final File directoryPath) {
        List<EMMChannel> channels = new ArrayList<>();
        File[] files = directoryPath.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });

        long itemCount = 0;
        long sizeInBytes = 0;
        long timeFirst = new Date().getTime();

        for (File f : files) {
            EMMChannel channel = EMMRssParser.getChannelForFile(f);
            itemCount += channel.getItems().size();
            sizeInBytes += f.length();

            channels.add(channel);
        }

        long timeSecond = new Date().getTime();
        long timeDiff = (timeSecond - timeFirst) / 1000;
        System.out.println("duration [s] = " + timeDiff);
        System.out.println("filesize [MB]= " + sizeInBytes / 1024 / 1024);
        System.out.println("channels     = " + channels.size());
        System.out.println("items        = " + itemCount);

        return channels.toArray(new EMMChannel[channels.size()]);
    }

    public static EMMChannel getChannelForFile(final File file) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(EMMRss.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            EMMRss rss = (EMMRss) jaxbUnmarshaller.unmarshal(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            EMMChannel channel = rss.getChannel();
            if (channel == null) {
                System.err.println(file.toString() + "does not have a channel");
            } else {
                channel.setFilename(file.getName());
                channel.setPathname(file.getPath());
                for (int i = 0; i < channel.getItems().size(); i++) {
                    channel.getItems().get(i).setPositionInChannel(i);
                }
            }

            return channel;
        } catch (JAXBException e) {
            System.err.println("corrupted file: " + file);
            e.printStackTrace();
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(EMMRssParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * This returns all files (no deep recursion) that are direct children of
     * the directory {@code dir}.
     *
     * @param dir
     * @return
     */
    public static File[] getFilesForDirectory(final File dir) {
        //return dir.listFiles();
        return FileUtils.listFiles(dir, new String[]{"xml"}, true).toArray(new File[0]);
    }

    private void runSimpleTest() {
        final File oneChannelFile = DEBUG_SAMPLE_FILE;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EMMRss.class
            );

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            EMMRss rss = (EMMRss) jaxbUnmarshaller.unmarshal(oneChannelFile);

            System.out.println(rss.getChannel().getItems().size());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
