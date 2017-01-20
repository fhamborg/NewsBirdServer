/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web.images;

import java.util.logging.Level;
import java.util.logging.Logger;
import spark.Request;
import spark.Response;
import static spark.Spark.get;

/**
 * Handles image download requests.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ImageController {

    private static final Logger LOG = Logger.getLogger(ImageController.class.getSimpleName());

    private static final String pathPrefix = "/images";

    public static void initRoutes() {
        get(pathPrefix + "/query", (Request req, Response res) -> {
            String query = req.queryParams("query");
            LOG.log(Level.INFO, "received new image query: ''{0}''", query);
            String imgName = ImageSearch.sendQueryAndDownloadFile(query);
            return "{\"imgPath\":\"" + imgName + "\"}";
        });
    }

}
