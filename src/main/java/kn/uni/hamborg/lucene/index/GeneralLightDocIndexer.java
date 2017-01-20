/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.lucene.index;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.utils.IndexUtils;
import net.sf.extjwnl.data.IndexWord;
import org.apache.lucene.index.IndexWriter;

/**
 * General purpose class to create a Lucene Index containing given LightDocs.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class GeneralLightDocIndexer {
    
    private static final Logger LOG = Logger.getLogger(GeneralLightDocIndexer.class.getSimpleName());

    /**
     * Creates a Lucene Index.
     *
     * @param indexDir Where the index is created.
     * @param docs Which docs it will contain.
     * @throws java.io.IOException
     */
    public static void createIndexWithLightDocs(File indexDir, List<LightDoc> docs) throws IOException {
        try (IndexWriter writer = IndexUtils.createIndexWriter(IndexUtils.openDirectory(indexDir))) {
            for (LightDoc doc : docs) {
                writer.addDocument(doc.asLuceneDocument());
            }
            LOG.info("successfully created new Index with " + docs.size() + " docs");
            
        }
    }
}
