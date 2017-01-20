/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.lucene.others;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.data.emm.EMMChannel;
import kn.uni.hamborg.data.emm.EMMRssParser;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.data.light.LightDocConverter;
import kn.uni.hamborg.language.Language;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class NamedEntityFinder {

    private final Language language;
    private final NameFinderME nameFinder;

    public NamedEntityFinder(Language language) {
        this.language = language;
        this.nameFinder = createNameFinderModel();

    }

    /**
     * Extracts the (currently only persons) named entities from tokens of a
     * single sentence.
     *
     * @param tokensOfSentence
     * @return
     */
    public Span[] extractNames(String[] tokensOfSentence) {
        return nameFinder.find(tokensOfSentence);
    }

    private NameFinderME createNameFinderModel() {
        File modelFile = null;
        switch (language) {
            case EN:
                modelFile = new File("models/en-ner-person.bin");
                break;
            default:
                throw new IllegalArgumentException("only en supported yet");
        }

        InputStream is = null;
        try {
            is = new FileInputStream(modelFile);
            TokenNameFinderModel model = new TokenNameFinderModel(is);
            return new NameFinderME(model);

        } catch (Exception ex) {
            Logger.getLogger(NamedEntityFinder.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            NamedEntityFinder.runTest();
        } catch (Exception ex) {
            Logger.getLogger(NamedEntityFinder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void runTest() throws Exception {
        final StandardAnalyzer analyzer = new StandardAnalyzer();
        final IndexWriterConfig config = new IndexWriterConfig( analyzer);
        final Directory directory = new RAMDirectory();
        final IndexWriter indexWriter = new IndexWriter(directory, config);

        final EMMChannel channel = EMMRssParser.getChannelForFile(EMMRssParser.DEBUG_SAMPLE_FILE);
        final LightDoc[] docs = LightDocConverter.convert(channel);

        for (LightDoc doc : docs) {
            indexWriter.addDocument(doc.asLuceneDocument());
        }

    }
}
