package kn.uni.hamborg.lucene.others;

import java.io.IOException;
import java.util.*;

import org.apache.commons.math3.linear.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.*;

/**
 * http://stackoverflow.com/questions/1844194/get-cosine-similarity-between-two-documents-in-lucene
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CosineDocumentSimilarity {

    private final Set<String> terms = new HashSet<>();
    private final RealVector v1;
    private final RealVector v2;
    private final String fieldname;

    protected CosineDocumentSimilarity(final IndexReader reader, int docid1, int docid2, final String fieldname) throws IOException {
        this.fieldname = fieldname;
        Map<String, Double> f1 = getTermFrequencies(reader, docid1);
        Map<String, Double> f2 = getTermFrequencies(reader, docid2);
        v1 = toRealVector(f1);
        v2 = toRealVector(f2);
    }

    protected double getCosineSimilarity() {
        return (v1.dotProduct(v2)) / (v1.getNorm() * v2.getNorm());
    }

    public static double getCosineSimilarity(final IndexReader reader, int docid1, int docid2, final String fieldname)
            throws IOException {
        return new CosineDocumentSimilarity(reader, docid1, docid2, fieldname).getCosineSimilarity();
    }

    Map<String, Double> getTermFrequencies(IndexReader reader, int docId)
            throws IOException {
        int numDocs = reader.numDocs();
        Terms vector = reader.getTermVector(docId, fieldname);
        TermsEnum termsEnum = null;
        termsEnum = vector.iterator(termsEnum);
        Map<String, Double> frequencies = new HashMap<>();
        BytesRef text = null;
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            long tf = (long) termsEnum.totalTermFreq();
            int df = reader.docFreq(new Term(fieldname, termsEnum.term()));

            frequencies.put(term, Math.sqrt(tf) * (1 + Math.log(numDocs / (df + 1))));
            terms.add(term);
        }
        return frequencies;
    }

    RealVector toRealVector(Map<String, Double> map) {
        RealVector vector = new ArrayRealVector(terms.size());
        int i = 0;
        for (String term : terms) {
            double value = map.containsKey(term) ? map.get(term) : 0;
            vector.setEntry(i++, value);
        }
        return (RealVector) vector.mapDivide(vector.getL1Norm());
    }
}
