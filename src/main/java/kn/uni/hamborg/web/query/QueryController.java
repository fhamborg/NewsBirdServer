/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web.query;

import java.util.logging.Logger;
import kn.uni.hamborg.adv.AnalysisProcessor;
import kn.uni.hamborg.web.MainController;
import kn.uni.hamborg.web.TableToWebConverter;
import static spark.Spark.*;

/**
 * This provides functionality to let the client send queries to the server.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class QueryController {

    private static final Logger LOG = Logger.getLogger(QueryController.class.getSimpleName());

    private static final String urlPrefix = "/query";

    public static void initRoutes(AnalysisProcessor analysisProcessor) {
        get(urlPrefix, (req, res) -> {
            QueryCommand.QueryCommandWrapper wrappedQuery = MainController.JSON_TRANSFORMER.getGson().fromJson(req.queryParams("query"), QueryCommand.QueryCommandWrapper.class);
            QueryCommand queryCommand = QueryCommand.fromWrapper(wrappedQuery,
                    analysisProcessor.getAnalysisWorkflow().getQueryParser(),
                    analysisProcessor.getAnalysisWorkflow().getIndexReader(),
                    analysisProcessor.getAnalysisWorkflow().getAnalyzer());
            LOG.info(queryCommand.toString());
            analysisProcessor
                    .withQuery(queryCommand)
                    .buildTable()
                    .computeTopics()
                    .computeSummaries();
            //   .computeTokenScoresByParser();

            return new TableToWebConverter(analysisProcessor.getAnalysisWorkflow().getTableManager()).getFullMatrixInformation();
        });

        get(urlPrefix + "/options", (req, res) -> {
            return new QueryOptions();
        }, MainController.JSON_TRANSFORMER);
    }
}
