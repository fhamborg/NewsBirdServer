/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kn.uni.hamborg.lucene.search;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.LuceneConfig;
import kn.uni.hamborg.data.emm.EMMItemReader;
import kn.uni.hamborg.data.light.LightDoc;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.joda.time.DateTime;

/**
 *
 * @author felix
 */
public class Searcher {

    private static Logger LOG = Logger.getLogger(Searcher.class.getName());

    public static void main(String[] args) {
        try {
            new Searcher().runTest();
        } catch (Exception ex) {
            Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    final Directory dir;
    final IndexReader ir;
    final IndexSearcher is;

    public Searcher() throws Exception {
        dir = FSDirectory.open(LuceneConfig.INDEX_DIR_DEFAULT.toPath());
        ir = DirectoryReader.open(dir);
        is = new IndexSearcher(ir);
        /*  is.setSimilarity(new DefaultSimilarity() {
            
         @Override
         public float sloppyFreq(int distance) {
         return 1;
         }
            
         @Override
         public float coord(int overlap, int maxOverlap) {
         return 1;
         }
            
         @Override
         public float lengthNorm(FieldInvertState state) {
         return 1;
         }
            
         @Override
         public float queryNorm(float sumOfSquaredWeights) {
         return 1;
         }
            
         });*/

    }

    public ScoreDoc[] search(String required, String should) {
        if (should.length() < 3) {
            return new ScoreDoc[]{};
        }

        BooleanQuery query = new BooleanQuery();

        for (String cur : required.split(" ")) {
            query.add(new TermQuery(new Term(LightDoc.CONTENT, cur)), Occur.MUST);
            System.out.println("must: " + cur);
        }

        for (String cur : should.split(" ")) {
            query.add(new TermQuery(new Term(LightDoc.CONTENT, cur)), Occur.SHOULD);
            System.out.println("should: " + cur);
        }

        try {
            TopDocs hits = is.search(query, 1000);
            return hits.scoreDocs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void printScoreDocsGui(ScoreDoc[] scoreDocs) {
        ScoreDocShower shower = new ScoreDocShower();
        shower.showResults(is, scoreDocs);
    }

    public void printScoreDocs(ScoreDoc[] scoreDocs) {
        for (int i = 0; i < scoreDocs.length; i++) {
            ScoreDoc scoreDoc = scoreDocs[i];
            try {
                Document first = is.doc(scoreDocs[0].doc);
                Document doc = is.doc(scoreDoc.doc);

                //   double d = CosineDocumentSimilarity.getCosineSimilarity(ir, scoreDocs[0].doc, scoreDoc.doc, LightDoc.CONTENT);
                if (EMMItemReader.filename.equals(doc.get(LightDoc.CHANNEL_FILE))
                        && EMMItemReader.pos == new Integer(doc.get(LightDoc.CHANNEL_POSITION))) {
                    //    System.out.println(EMMItemReader.readEMMItem(doc).getText());
                }
                System.out.println(
                        "" + i + ": "
                        + new Date(new Long(doc.get(LightDoc.PUB_DATE))) + ": "
                        //  + d + " - "
                        + scoreDoc.score + " - "
                        + doc.get(LightDoc.PUB_COUNTRY) + " - "
                        + doc.get(LightDoc.TITLE) + " - "
                        + doc.get(LightDoc.CHANNEL_FILE) + " - "
                        + doc.get(LightDoc.CHANNEL_POSITION) + " - "
                );
            } catch (IOException io) {
                io.printStackTrace();
            }
            // EMMItem item = EMMItemReader.readEMMItem(doc);
            // System.out.println(item.toString());
        }
    }

    public void runTest() {
        try {
            /* Map<String, Analyzer> fieldAnalyzers = new HashMap<>();
             fieldAnalyzers.put(LightDoc.CONTENT_STEMMED, new EnglishAnalyzer());
             PerFieldAnalyzerWrapper customAnalyzer
             = new PerFieldAnalyzerWrapper(
             new StandardAnalyzer(), fieldAnalyzers);
             */
            /* QueryParser parser = new QueryParser(
             LightDoc.TITLE,
             new StandardAnalyzer(
             Version.LATEST));*/
            BooleanQuery query = new BooleanQuery();

            TermQuery tq1 = new TermQuery(new Term(LightDoc.CONTENT, "russia"));
            tq1.setBoost(4f);
            query.add(tq1, Occur.MUST);
            TermQuery tq2 = new TermQuery(new Term(LightDoc.CONTENT, "ukraine"));
            tq2.setBoost(4f);
            query.add(tq2, Occur.MUST);
            TermQuery tq3 = new TermQuery(new Term(LightDoc.CONTENT, "troops"));
            tq3.setBoost(1f);
            query.add(tq3, Occur.SHOULD);
            TermQuery tq4 = new TermQuery(new Term(LightDoc.CONTENT, "border"));
            tq3.setBoost(1f);
            query.add(tq4, Occur.SHOULD);
        //  TermQuery tq5 = new TermQuery(new Term(LightDoc.PUB_COUNTRY, "RU"));
            //  query.add(tq5, Occur.MUST);

            //   Query query = parser.parse("russia troops border");
            DateTime dtstart = new DateTime(2014, 11, 7, 0, 0);
            DateTime dtend = new DateTime(2014, 11, 9, 0, 0);
            NumericRangeFilter<Long> nrf = NumericRangeFilter.newLongRange(LightDoc.PUB_DATE,
                    dtstart.getMillis(), dtend.getMillis(),
                    true, true);
            TopDocs hits = is.search(query, nrf, 1000);
            System.out.println("opening gui");
            printScoreDocsGui(hits.scoreDocs);
            //printScoreDocs(hits.scoreDocs);

            // get all docs
        /*    Bits liveDocs = MultiFields.getLiveDocs(ir);
             for (int i = 0; i < ir.maxDoc(); i++) {
             if (liveDocs != null && !liveDocs.get(i)) {
             continue;
             }

             Document doc = ir.document(i);
             nonEventArticles.add(doc);
             }

             // remove all event articles, so that we only have event articles
             nonEventArticles.removeAll(eventArticles);

             System.out.println("# e-articles = " + eventArticles.size());
             System.out.println("# ne-articles = " + nonEventArticles.size());
             // try to get more like one of the top results (that are an EA)
             MoreLikeThis mlt = new MoreLikeThis(ir);
             mlt.setFieldNames(new String[]{LightDoc.TITLE});
             String[] terms = mlt.retrieveInterestingTerms(hits.scoreDocs[3].doc);
             System.out.println("interesting terms");
             for (String t : terms) {
             System.out.println(t);
             }
             Query likeThis = mlt.like(hits.scoreDocs[3].doc);
             DateTime dtstart = new DateTime(2014, 11, 6, 0, 0);
             DateTime dtend = new DateTime(2014, 11, 9, 0, 0);
             NumericRangeFilter<Long> nrf = NumericRangeFilter.newLongRange(LightDoc.PUB_DATE,
             dtstart.getMillis(), dtend.getMillis(),
             true, true);
             TopDocs hitsLikeThis = is.search(likeThis, nrf, 100);

             printScoreDocs(hitsLikeThis.scoreDocs);*/
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
