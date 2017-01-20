/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import kn.uni.hamborg.lucene.analyzer.AnalyzerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.util.Bits;

/**
 * Provides practical methods to create
 * {@link IndexReader}, {@link IndexWriter}, {@link IndexSearcher} and others
 * very conveniently and fast.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class IndexUtils {
    
    private static final Logger LOG = Logger.getLogger(IndexUtils.class.getSimpleName());

    /**
     * Creates a default IndexWriterConfig with our custom {@link Analyzer} from
     * {@link  AnalyzerFactory}.
     *
     * @return
     */
    public static IndexWriterConfig createIndexWriterConfig() {
        return new IndexWriterConfig(AnalyzerFactory.createCustomAnalyzer());
    }

    /**
     * Creates a default {@link IndexWriter} for the specified directory
     * {@code dir}. See also {@link #createIndexWriterConfig() }.
     *
     * @param dir
     * @return
     * @throws IOException
     */
    public static IndexWriter createIndexWriter(final Directory dir) throws IOException {
        return new IndexWriter(dir, createIndexWriterConfig());
    }

    /**
     * Creates a RAMDirectory.
     *
     * @return
     */
    public static RAMDirectory createTemporaryDir() {
        final RAMDirectory ramdir = new RAMDirectory();
        return ramdir;
    }

    /**
     * Creates a RAMDirectory which already contains the {@link Document}s that
     * are given.
     *
     * @param initialDocs
     * @return
     * @throws IOException
     */
    public static RAMDirectory createTemporaryDir(final Document[] initialDocs) throws IOException {
        final RAMDirectory dir = createTemporaryDir();
        final IndexWriter iw = createIndexWriter(dir);
        for (Document doc : initialDocs) {
            iw.addDocument(doc);
        }
        
        LOG.log(Level.INFO, "created RAMDirectory with {0} documents", iw.numDocs());
        iw.close();
        
        return dir;
    }
    
    public static IndexReader createIndexReader(final Directory dir) throws IOException {
        final IndexReader ir = DirectoryReader.open(dir);
        LOG.log(Level.INFO, "opened Index with {0} documents", ir.numDocs());
        return ir;
    }
    
    public static IndexSearcher createIndexSearcher(final Directory dir) throws IOException {
        return new IndexSearcher(createIndexReader(dir));
    }
    
    public static IndexSearcher createIndexSearcher(final IndexReader reader) {
        return new IndexSearcher(reader);
    }

    /**
     * Opens a Lucene {@link Directory}.
     *
     * @param dir
     * @return
     * @throws IOException
     */
    public static Directory openDirectory(final File dir) throws IOException {
        final Directory luceneDir = FSDirectory.open(dir.toPath());
        LOG.log(Level.INFO, "opened Directory {0} successfully", dir);
        return luceneDir;
        
    }

    /**
     * this will create a temporary Directory in RAM and load all documents from
     * {@code dir} into this temporary Directory which is then returned instead.
     * Note, that any changes will not be saved to {@code dir} as they only
     * happen in the temporary Directory.
     *
     * This also closes the original Directory {@code luceneDir}.
     *
     * @param luceneDir
     * @return
     * @throws IOException
     */
    public static RAMDirectory loadInRAMDirectory(final FSDirectory luceneDir) throws IOException {
        /*  final IndexReader ir = createIndexReader(luceneDir);
         final Bits liveDocs = MultiFields.getLiveDocs(ir);

         // create tmp dir
         final RAMDirectory tmpDir = createTemporaryDir();
         final IndexWriter tmpWriter = createIndexWriter(tmpDir);
         int docCount = 0;
         long starttime = new DateTime().getMillis();

         LOG.info("Directory will be loaded completely in a temporary RAMDirectory for increased performance. This can take a while.\n"
         + "Note that any changes to this Directory are not persisted!");

         // add docs to tmp dir
         for (int i = 0; i < ir.maxDoc(); i++) {
         Document doc = ir.document(i);
         if (liveDocs != null && !liveDocs.get(i)) {
         // this document does not exist
         continue;
         }

         tmpWriter.addDocument(doc);
         docCount++;
         }

         ir.close();
         luceneDir.close();
         tmpWriter.close();
         LOG.info("created RAMDirectory with " + docCount + " documents in " + ((new DateTime().getMillis() - starttime) / 1000)
         + "s; in memory size = " + (tmpDir.ramBytesUsed() / 1024 / 1024) + "MB");

         return tmpDir; */
        return new RAMDirectory(luceneDir, IOContext.DEFAULT);
    }
    
    public static Document[] getDocsByDocId(IndexReader r, int[] ids) throws IOException {
        Document[] docs = new Document[ids.length];
        for (int i = 0; i < ids.length; i++) {
            int id = ids[i];
            docs[i] = r.document(id);
        }
        return docs;
    }
    
    public static Document[] getDocsByDocId(IndexReader r, List<Integer> ids) throws IOException {
        int[] tmp = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            int id = ids.get(i);
            tmp[i] = id;
        }
        
        return getDocsByDocId(r, tmp);
    }

    /**
     *
     * @param indexSearcher
     * @param fieldname
     * @param match
     * @return
     */
    public static int[] getDocIDsWithExactMatch(IndexSearcher indexSearcher, String fieldname, String match) {
        try {
            TopDocs topdocs = indexSearcher.search(
                    new TermQuery(new Term(fieldname, match)), Integer.MAX_VALUE);
            System.out.println("found " + topdocs.totalHits + " docs");
            return getDocIDsFromTopDocs(topdocs);
        } catch (IOException ex) {
            return null;
        }
    }
    
    public static int[] getDocIDsFromTopDocs(TopDocs topdocs) {
        int[] docids = new int[topdocs.scoreDocs.length];
        for (int i = 0; i < topdocs.scoreDocs.length; i++) {
            int docid = topdocs.scoreDocs[i].doc;
            docids[i] = docid;
        }
        return docids;
    }
    
    public static int[] getAllDocIDsFromIndex(IndexReader indexReader) {
        List<Integer> docsids = new ArrayList<>();
        Bits liveDocs = MultiFields.getLiveDocs(indexReader);
        for (int i = 0; i < indexReader.maxDoc(); i++) {
            if (liveDocs != null && !liveDocs.get(i)) {
                continue;
            }
            
            docsids.add(i);
        }
        
        return ArrayUtils.toPrimitive(docsids.toArray(new Integer[0]));
    }
    
}
