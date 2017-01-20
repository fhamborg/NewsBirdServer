/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web;

import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.AnalysisProcessor;
import kn.uni.hamborg.adv.AnalysisWorkflow;
import kn.uni.hamborg.config.PublicConfig;
import kn.uni.hamborg.web.cell.CellInformationController;
import kn.uni.hamborg.web.images.ImageController;
import kn.uni.hamborg.web.query.QueryController;
import spark.Request;
import spark.Response;
import static spark.Spark.*;

/**
 * This provides functionality to handle main and central communication between
 * client and server. It also initializes the Spark server-side framework to
 * enable CORS.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class MainController {

    private static final Logger LOG = Logger.getLogger(MainController.class.getSimpleName());

    public static final JsonTransformer JSON_TRANSFORMER = new JsonTransformer();
    // public static final Gson GSON = new Gson();

    private static final AnalysisWorkflow workflow = AnalysisWorkflow.createDefaultWorkflow();
    private static final AnalysisProcessor analysisProcessor = new AnalysisProcessor(workflow);

    public static void main(String[] args) throws Exception {
        externalStaticFileLocation(PublicConfig.publicBase.getAbsolutePath());
        LOG.log(Level.INFO, "making public: {0}", PublicConfig.publicBase);
        setPort(8080);
        enableCORS("*", "*", "*");

        QueryController.initRoutes(analysisProcessor);
        CellInformationController.initRoutes(analysisProcessor);
        ImageController.initRoutes();
        ArticleController.initRoutes(analysisProcessor);
    }

    /**
     * This is used to enable Cross-Origin Resource Sharing. <br>
     *
     * See here:
     * http://yobriefca.se/blog/2014/02/20/spas-and-enabling-cors-in-spark/
     *
     * @param origin
     * @param methods
     * @param headers
     */
    private static void enableCORS(final String origin, final String methods, final String headers) {
        before((Request request, Response response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
        });
    }

}
