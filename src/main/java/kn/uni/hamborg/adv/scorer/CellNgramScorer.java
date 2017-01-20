/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.scorer;

import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.table.TableManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CellNgramScorer {

    private static final Logger LOG = Logger.getLogger(CellNgramScorer.class.getSimpleName());

    private final TableManager tableManager;
    private final IndexSearcher indexSearcher;
    private final BerkeleyLMScorer lmScorer;
    private final String fieldname;

    public CellNgramScorer(TableManager tableManager, IndexSearcher indexSearcher, String fieldname) {
        this.tableManager = tableManager;
        this.indexSearcher = indexSearcher;
        this.lmScorer = new BerkeleyLMScorer();
        this.fieldname = fieldname;
    }

    /**
     * Compute the language model on the contents of the matrix.
     */
    public void computeModelOnMatrix() {
        try {
            final Query groundQuery = tableManager.getFilterQuery();
            //System.out.println(groundQuery);
            final ScoreDoc[] groundDocs = indexSearcher.search(groundQuery, Integer.MAX_VALUE).scoreDocs;
            LOG.log(Level.INFO, "found {0} docs with query {1}", new Object[]{groundDocs.length, groundQuery.toString()});

            for (ScoreDoc scoreDoc : groundDocs) {
                Document doc = indexSearcher.doc(scoreDoc.doc);
                lmScorer.indexText(doc.get(fieldname));
            }
            LOG.log(Level.INFO, "created LM on {0} documents [{1}]", new Object[]{groundDocs.length, fieldname});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates the score of a given text. In order to get comparable scores,
     * the input should be of the same unit, e.g., always 1 sentence.
     *
     * @param sentence
     * @return
     */
    public float calcTextScore(String sentence) {
        return lmScorer.getTextScore(sentence);
    }
}
