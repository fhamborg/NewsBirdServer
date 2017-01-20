/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.lucene.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ScoreDocToDocConverter {

    private static final Logger LOG = Logger.getLogger(ScoreDocToDocConverter.class.getSimpleName());

    /**
     * Converts all ScoreDocs to Documents. If a very large number of ScoreDocs
     * are given, this function should not be used due to memory restrictions.
     * Instead it is then recommended to process these ScoreDocuments one by
     * one.
     */
    public static Document[] toDocuments(final IndexReader indexReader, final ScoreDoc[] scoreDocs) {
        try {
            List<Document> docs = new ArrayList<>();
            for (ScoreDoc scoreDoc : scoreDocs) {
                docs.add(indexReader.document(scoreDoc.doc));
            }
            return docs.toArray(new Document[docs.size()]);
        } catch (IOException ex) {
            Logger.getLogger(ScoreDocToDocConverter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
