/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.utils;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import kn.uni.hamborg.lucene.field.VecTextField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class DocumentUtils {

    private static final Logger LOG = Logger.getLogger(DocumentUtils.class.getSimpleName());

    public static final String virtualFieldname = "_virtualField_";

    /**
     * Creates one String out of all the documents defined by their id in this
     * index. Documents' content is separated by linebreak.
     *
     * @param indexReader
     * @param ids
     * @param fieldname
     * @return
     * @throws IOException
     */
    public static String getConcatenatedContent(IndexReader indexReader, List<Integer> ids, String fieldname) throws IOException {
        final Document[] docs = IndexUtils.getDocsByDocId(indexReader, ids);
        return getConcatenatedContent(docs, fieldname);
    }

    /**
     * Creates one String out of all the documents. Documents' content is
     * separated by linebreak.
     *
     * @param docs
     * @param fieldname
     * @return
     */
    public static String getConcatenatedContent(Document[] docs, String fieldname) {
        final StringBuilder sb = new StringBuilder();
        for (Document doc : docs) {
            sb.append(doc.get(fieldname));
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }

    /**
     * Creates a new {@link Document} with only one field with the fieldname
     * {@link #virtualFieldname} which has {@code s} as its content.
     *
     * @param s
     * @return
     */
    public static Document createDocumentFromString(String s) {
        Document d = new Document();
        d.add(new VecTextField(virtualFieldname, s, Field.Store.YES));
        return d;
    }

    public static String[] getIdsFromDocs(Document[] docs) {
        String[] ids = new String[docs.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = LightDocUtils.getId(docs[i]);
        }
        return ids;
    }

}
