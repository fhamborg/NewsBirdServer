/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.AnalysisProcessor;
import kn.uni.hamborg.data.light.LightDoc;
import kn.uni.hamborg.utils.IndexUtils;
import kn.uni.hamborg.utils.LightDocUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import static spark.Spark.get;

/**
 * Provides functionality to get articles.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ArticleController {

    private static final Logger LOG = Logger.getLogger(ArticleController.class.getSimpleName());

    private static final String pathPrefix = "/articles";

    /**
     * Returns null if the article is not found.
     *
     * @param analysisProcessor
     * @param id
     * @return
     * @throws IOException
     */
    private static Article getArticleFromId(AnalysisProcessor analysisProcessor, String id) throws IOException {
        final Query q = new TermQuery(new Term(LightDoc.ID, id));
        try {
            final int docId = analysisProcessor.getAnalysisWorkflow().getIndexSearcher().search(q, 1).scoreDocs[0].doc;
            final Document doc = IndexUtils.getDocsByDocId(analysisProcessor.getAnalysisWorkflow().getIndexReader(), new int[]{docId})[0];
            return new Article(LightDocUtils.getTitle(doc),
                    LightDocUtils.getContent(doc),
                    LightDocUtils.getDescription(doc),
                    LightDocUtils.getPubUrl(doc),
                    LightDocUtils.getChannelGuid(doc),
                    LightDocUtils.getPubDate(doc)
            );
        } catch (ArrayIndexOutOfBoundsException e) {
            if (!id.equals("efi")) {
                LOG.log(Level.SEVERE, "article id {0} was not found", id);
            }
            //throw e;
            return null;
        }
    }

    public static void initRoutes(AnalysisProcessor analysisProcessor) {
        get(pathPrefix + "/:id", (req, res) -> {
            final String json = req.params(":id");

            try {
                JsonArray articleIds = new JsonParser().parse(json).getAsJsonArray();
                List<Article> articles = new ArrayList<>();
                for (JsonElement e : articleIds) {
                    String id = e.getAsString();
                    id = id.substring(3, id.length() - 3);

                    articles.add(getArticleFromId(analysisProcessor, id));
                }
                return articles;
            } catch (Exception e) {
                // it is a single value
                return getArticleFromId(analysisProcessor, json.substring(3, json.length() - 3));
            }

        }, MainController.JSON_TRANSFORMER);
    }
}
