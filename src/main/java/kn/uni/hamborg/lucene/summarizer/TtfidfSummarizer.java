/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.lucene.summarizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;

/**
 * Provides functionality to summarize Documents based on a simple TTF-IDF
 * approach.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TtfidfSummarizer extends Summarizer {

    private static final Logger LOG = Logger.getLogger(TtfidfSummarizer.class.getSimpleName());

    private final TFIDFSimilarity tfidfSimilarity = new DefaultSimilarity();

    public TtfidfSummarizer(IndexReader indexReader, IndexSearcher indexSearcher, Analyzer analyzer) {
        super(indexReader, indexSearcher, analyzer);
    }

    public TtfidfSummarizer(Document[] documents, Analyzer analyzer) throws IOException {
        super(documents, analyzer);
    }

    public TtfidfSummarizer(String[] contents, Analyzer analyzer) throws IOException {
        super(contents, analyzer);
    }

    /**
     * Calculates the TTF-IDF value for each term aggregated on all documents,
     * i.e., the document frequency is inversed and instead of using the
     * document-wise term frequency (TF) we use the total term frequency (TTF),
     * i.e., how often this term occurs in all documents.
     *
     * @param field The field which terms' TTFIDF should be calculated.
     * @return A map consisting of term-TTFIDF pairs.
     * @throws IOException
     */
    @Override
    protected Map<String, Double> calculateTermScore(final String field) throws IOException {
        Terms terms = MultiFields.getTerms(indexReader, field);
        TermsEnum termEnum = terms.iterator(null);
        BytesRef term = null;
        Map<String, Double> termTfidf = new HashMap<>();
        int docCount = indexReader.numDocs();

        while ((term = termEnum.next()) != null) {
            String termText = term.utf8ToString();
            Term termInstance = new Term(field, term);
            // term and doc frequency in all documents
            long indexTf = indexReader.totalTermFreq(termInstance);
            long indexDf = indexReader.docFreq(termInstance);
            // term frequency in this document
            // long curTf = itr.totalTermFreq();
            double tfidf = tfidfSimilarity.tf(indexTf) * tfidfSimilarity.idf(indexDf, docCount);

            // TODO actually it would be better to calculate tfidf per document (document tf and index df) 
            termTfidf.put(termText, tfidf);
        }

        return termTfidf;
    }

}
