/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.lucene.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.data.emm.EMMChannel;
import kn.uni.hamborg.data.emm.EMMItem;
import kn.uni.hamborg.data.emm.EMMRssParser;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.data.light.LightDocConverter;
import kn.uni.hamborg.language.Language;
import kn.uni.hamborg.language.translator.EasyTranslator;
import kn.uni.hamborg.language.translator.LightDocTranslator;
import kn.uni.hamborg.lucene.analyzer.AnalyzerFactory;
import kn.uni.hamborg.utils.ThreadUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Class which allows to create an index from a directory containing EMM's XML
 * files.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EMMDataSetIndexer {

    private static final Logger log = Logger.getLogger(EMMDataSetIndexer.class.getSimpleName());

    enum LanguageMode {

        TRANSLATE_NON_EN_TO_EN, INDEX_NON_EN, DISCARD_NON_EN
    };

    /**
     * Defines what happens with non english articles.
     */
    private static final LanguageMode languageMode = LanguageMode.TRANSLATE_NON_EN_TO_EN;

    private int countTranslatedLightDocs = 0;

    public static void main(String[] args) {
        try {
            new EMMDataSetIndexer().createIndexMultiThreaded(
                    LuceneConfig.DATA_DIR_15_06_TO_07,
                    LuceneConfig.INDEX_DIR_15_06_TO_07,
                    //LuceneConfig.DATA_DIR_14_11_04_TO_11,
                    //LuceneConfig.INDEX_DIR_14_11_04_TO_11_ALL_LANG_TO_EN,
                    //LuceneConfig.DATA_DIR_2014,
                    //LuceneConfig.INDEX_DIR_2014_WITH_NON_EN,
                    false);
        } catch (Exception ex) {
            log.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private long itemCount = 0;
    private long fileFinishedCount = 0;
    private long fileIgnoredCount = 0;
    private long fileNonEnglishCount = 0;
    private IndexWriter indexWriter;

    /**
     * Runnable class that converts a {@link File} into its {@link EMMItem}s
     * which are then added as {@link Document}s to the Lucene Index.
     */
    private class IndexerRunnable implements Runnable {

        private final File file;
        private final boolean isBinary;

        public IndexerRunnable(final File file, boolean isBinary) {
            this.file = file;
            this.isBinary = isBinary;
        }

        private void indexLightDocs(LightDoc[] docs) {
            for (LightDoc doc : docs) {
                try {
                    // System.out.println(doc.toString());
                    indexWriter.addDocument(doc.asLuceneDocument());
                    itemCount++;
                } catch (IOException ioe) {
                    log.logp(Level.SEVERE, EMMDataSetIndexer.class.getSimpleName(), "addToIndex", ioe.getMessage(), ioe);
                }
            }
            //log.info("" + itemCount);
        }

        private void processBinary() {
            try {
                final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                final List<LightDoc> lightDocs = (List<LightDoc>) ois.readObject();
                ois.close();

                indexLightDocs(lightDocs.toArray(new LightDoc[0]));
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(EMMDataSetIndexer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void processXml() {
            final EMMChannel emmChannel = EMMRssParser.getChannelForFile(file);
            boolean needToTranslate = false;
            if (!emmChannel.getLanguage().toLowerCase().equals(Language.EN.toString().toLowerCase())) {
                // channel is non english
                switch (languageMode) {
                    case DISCARD_NON_EN:
                        // and we dont want to translate it, so skip it
                        fileIgnoredCount++;
                        return;
                    case INDEX_NON_EN:
                        // just index it as it is
                        fileNonEnglishCount++;
                        break;
                    case TRANSLATE_NON_EN_TO_EN:
                        // but we want to translate it
                        fileNonEnglishCount++;
                        needToTranslate = true;
                        break;
                }
            }

            LightDoc[] docs = LightDocConverter.convert(emmChannel);

            // if the documents need to be translated at first, do this now
            if (needToTranslate) {
                for (int i = 0; i < docs.length; i++) {
                    docs[i] = LightDocTranslator.translateToEnglish(docs[i]);
                    countTranslatedLightDocs++;
                }
            }

            // index the documents (here we assume, i.e., import is optimized for, 
            // that they are in english, but also allow other languages)
            indexLightDocs(docs);
        }

        @Override
        public void run() {
            if (isBinary) {
                processBinary();
            } else {
                processXml();
            }
            fileFinishedCount++;
        }
    }

    /**
     * Creates a Lucene Index by reading in {@link File}s from the
     * {@code dataDir} directory. Is multithreaded (currently 4 threads are used
     * in a pool).
     *
     * @param dataDir Where the files will be read from.
     * @param indexDir Where the index will be created.
     * @param isBinary
     * @throws Exception
     */
    public void createIndexMultiThreaded(File dataDir, File indexDir, boolean isBinary) throws Exception {
        if (indexDir.exists()) {
            throw new RuntimeException("indexDir already exists, aborting! " + indexDir);
        }

        final Analyzer customAnalyzer = AnalyzerFactory.createCustomAnalyzer();
        final ExecutorService executorService = Executors.newFixedThreadPool(LuceneConfig.INDEX_NUMBER_OF_THREADS);
        log.log(Level.INFO, "created executorService with {0} threads", LuceneConfig.INDEX_NUMBER_OF_THREADS);

        final Directory directory = FSDirectory.open(indexDir.toPath());
        final IndexWriterConfig config = new IndexWriterConfig(customAnalyzer);
        indexWriter = new IndexWriter(directory, config);

        log.log(Level.INFO, "{0} exists = {1}", new Object[]{dataDir.toString(), dataDir.exists()});
        log.log(Level.INFO, "saving index to {0}. exists already = {1}", new Object[]{indexDir.toString(), indexDir.exists()});

        File[] filesInDir;
        if (isBinary) {
            filesInDir = dataDir.listFiles();
        } else {
            filesInDir = EMMRssParser.getFilesForDirectory(dataDir);
        }

        // filesInDir = new File[]{new File("C:\\priv\\mp-local\\04-nov-2014\\incoming-1415056141565-lapresse-CA.xml")};
        log.log(Level.INFO, "found {0} files", filesInDir.length);
        log.info("indexing...");
        long startTime = new Date().getTime();
        final DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(0);

        for (File fileInDir : filesInDir) {
            IndexerRunnable indexerRunnable = new IndexerRunnable(fileInDir, isBinary);
            executorService.execute(indexerRunnable);
        }

        executorService.shutdown();
        log.info("started all threads, now waiting for all threads to finish");

        while (!executorService.isTerminated()) {
            float elapsedMinutes = (new Date().getTime() - startTime) / 1000f / 60;
            float countFilesTodo = filesInDir.length - fileFinishedCount;
            float filesPerMin = fileFinishedCount / elapsedMinutes;
            log.log(Level.INFO, "{0} of {1} ({2}" + " ignored); " + "finished {3}"
                    + "%; " + "{4}" + " files/minute; "
                    + "elapsed: {5}" + " minutes; " + "finished in: {6} minutes", new Object[]{fileFinishedCount, filesInDir.length, fileIgnoredCount, df.format(100.0 * fileFinishedCount / filesInDir.length), df.format(filesPerMin), df.format(elapsedMinutes), df.format(countFilesTodo / filesPerMin)});

            ThreadUtils.sleep(10000);
        }
        executorService.awaitTermination(20, TimeUnit.DAYS);

        indexWriter.close();

        log.log(Level.INFO, "finished indexing, indexed {0} documents", itemCount);
        log.log(Level.INFO, "ignored files: {0}, total files: {1}", new Object[]{fileIgnoredCount, fileFinishedCount});
        log.log(Level.INFO, "non english files: {0}", fileNonEnglishCount);
        log.log(Level.INFO, "translated {0} docs", countTranslatedLightDocs);
        log.log(Level.INFO, "text too large count (api requests) {0}", EasyTranslator.textTooLargeCount);
    }
}
