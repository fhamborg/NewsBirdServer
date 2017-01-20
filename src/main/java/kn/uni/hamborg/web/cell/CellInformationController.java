/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web.cell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.AnalysisConfiguration;
import kn.uni.hamborg.adv.AnalysisProcessor;
import kn.uni.hamborg.adv.scorer.CellNgramScorer;
import kn.uni.hamborg.adv.scorer.FelixScorer;
import kn.uni.hamborg.adv.scorer.POSScorer;
import kn.uni.hamborg.adv.scorer.SentenceLin2002SingleScorer;
import kn.uni.hamborg.adv.summary.CellStopWordRemover;
import kn.uni.hamborg.adv.summary.Summaries;
import kn.uni.hamborg.utils.StringScoreUtils;
import kn.uni.hamborg.adv.summary.Summary;
import kn.uni.hamborg.adv.table.FilterCell;
import kn.uni.hamborg.adv.table.TableManager;
import kn.uni.hamborg.language.analyzers.OpenNLPPosAnalyzer;
import kn.uni.hamborg.language.analyzers.OpenNLPTokenizer;
import kn.uni.hamborg.lucene.summarizer.ChunkExtractor;
import kn.uni.hamborg.lucene.summarizer.StringScore;
import kn.uni.hamborg.utils.DocumentUtils;
import kn.uni.hamborg.web.ClientSideOptions;
import kn.uni.hamborg.web.MainController;
import org.apache.lucene.document.Document;
import spark.Request;
import spark.Response;
import static spark.Spark.*;

/**
 * This class provides interfaces for the client / userinterface (NewsBird) for
 * cell related information retrieval, e.g., getting summary sentences.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CellInformationController {

    private static final Logger LOG = Logger.getLogger(CellInformationController.class.getSimpleName());

    private static final String pathPrefix = "/cells";

    public static void initRoutes(AnalysisProcessor analysisProcessor) {
        get(pathPrefix + "/options", (req, res) -> {
            return new CellInformationOptions();
        }, MainController.JSON_TRANSFORMER);

        get(pathPrefix + "/clientoptions", (req, res) -> {
            return new ClientSideOptions();
        }, MainController.JSON_TRANSFORMER);

        /**
         * Invoked if the client request information for the cells.
         */
        get(pathPrefix + "/getinfo", (Request req, Response res) -> {
            final TableManager tableManager = analysisProcessor.getAnalysisWorkflow().getTableManager();

            final CellInformationCommand options = MainController.JSON_TRANSFORMER.getGson().fromJson(req.queryParams("options"), CellInformationCommand.class);
            LOG.log(Level.INFO, "received info request: {0}", options);

            // update AnalysisConfiguration
            AnalysisConfiguration.summarization_Lin2002Single_StartingWithStigmaWords_ReducedScore = options.isSummarizationSentence_Lin2002Single_StartingWithStigmaWords_ReducedScore();

            final String attrKey = options.getAttributeKey();

            final List<Cell> cells = new ArrayList<>();

            final OpenNLPTokenizer tokenizer = new OpenNLPTokenizer();
            final OpenNLPPosAnalyzer posAnalyzer = new OpenNLPPosAnalyzer(tokenizer);
            final POSScorer posScorer = new POSScorer();
            final SentenceLin2002SingleScorer sentenceLinScorer = new SentenceLin2002SingleScorer();
            final ChunkExtractor chunkExtractor = new ChunkExtractor();
            final CellStopWordRemover cellStopWordRemover = new CellStopWordRemover(tableManager);
            final FelixScorer felixScorer = new FelixScorer();
            final CellNgramScorer cellNgramScorer = analysisProcessor.getAnalysisWorkflow().getCellNgramScorer();

            /**
             * According to Lin2002Single we reduce the score so that sentences
             * that start with a stigma word, are not likely to actually be the
             * top summary sentences.
             */
            final boolean startingWithStigmaLeadsToReducedScore = AnalysisConfiguration.summarization_Lin2002Single_StartingWithStigmaWords_ReducedScore;
            final boolean felixScorerActive = options.isSummarizationSentence_FelixScorer();

            for (FilterCell cell : tableManager.getCells()) {
                if (cell.getAttribute(attrKey) == null) {
                    LOG.log(Level.INFO, "need to compute information for cell {0}", cell);
                    List<StringScore> elements = new ArrayList<>();
                    List<StringScore> fullSentences = new ArrayList<>();
                    float sentenceScoreByMatrixLM = 0.0f;
                    final Summaries summaries = (Summaries) cell.getAttribute(Summaries.class);
                    final Summary summary = summaries.getSummaries().get(options.getSummaryField());
                    switch (options.getSummaryElement()) {
                        case SUMMARY_SENTENCES:
                            elements = Arrays.asList(summary.getTopSentences());
                            break;
                        case SUMMARY_TOKENS:
                            elements = Arrays.asList(summary.getTopTerms());
                            break;
                        case TOPIC_TOKENS:
                            elements = Arrays.asList(summary.getTopTermsOfTopics());
                            break;
                        default:
                            throw new RuntimeException();
                    }

                    // add some weighting
                    switch (options.getSummaryElement()) {
                        case SUMMARY_SENTENCES:
                            final int minTokensInSentence = options.getMinTokensInSentence();
                            final List<StringScore[]> listOfTokenizedSentences = new ArrayList<>();

                            int sentenceCount = 0;
                            for (StringScore scoredSentence : elements) {
                                double sentenceScore = scoredSentence.getScore();
                                String[] sentenceTokens = tokenizer.tokenizeSentence(scoredSentence.getValue());

                                if (AnalysisConfiguration.enabledCellNgramScorer) {
                                    // check whether we already have enough sentences for scoring
                                    if (sentenceCount++ < options.getNumberOfSentences()) {
                                        // calculate the score of this sentence with respect to the complete matrix
                                        sentenceScoreByMatrixLM += cellNgramScorer.calcTextScore(scoredSentence.getValue());
                                        //System.out.println("scoring " + sentenceCount + "th sentence '" + scoredSentence.getValue() + "' = " + sentenceScoreByMatrixLM);
                                    }
                                }

                                // if enabled, get the top chunks only
                                if (options.isSummarizationSentence_TopChunksOnly()) {
                                    sentenceTokens = chunkExtractor.extractMostImportantChunks(sentenceTokens);
                                }

                                final StringScore[] scoredSentenceTokens = new StringScore[sentenceTokens.length];

                                if (sentenceTokens.length < minTokensInSentence) {
                                    LOG.log(Level.INFO, "skipping sentence (#tokens={0}): {1}", new Object[]{sentenceTokens.length, scoredSentence.getValue()});
                                    continue;
                                }

                                // if active, apply Lin2002Single method for sentence scoring based on first token (& POS)
                                if (startingWithStigmaLeadsToReducedScore) {
                                    final String[] posTags = posAnalyzer.tagSentence(sentenceTokens);
                                    // System.out.println("" + sentenceScore + ": " + scoredSentence.getValue());
                                    sentenceScore = sentenceScore
                                            + Math.abs(sentenceScore) * sentenceLinScorer.computeRelativeScoreOfSentence(sentenceTokens, posTags);
                                    // System.out.println("" + sentenceScore + ": " + scoredSentence.getValue());
                                }

                                // if active, apply Felix Sentence Scorer
                                if (felixScorerActive) {
                                    // System.out.println("" + sentenceScore + ": " + scoredSentence.getValue());
                                    sentenceScore = sentenceScore
                                            + Math.abs(sentenceScore) * felixScorer.computeRelativeScoreOfSentence(sentenceTokens, null);
                                    // System.out.println("" + sentenceScore + ": " + scoredSentence.getValue());
                                }

                                // after the sentence has been tokenized, apply the sentence's score to each of its token 
                                for (int i = 0; i < sentenceTokens.length; i++) {
                                    scoredSentenceTokens[i] = new StringScore(sentenceTokens[i], sentenceScore);
                                }

                                // ... and add all tokens to the list
                                listOfTokenizedSentences.add(scoredSentenceTokens);

                                // also put the full sentence as it is to the list
                                fullSentences.add(new StringScore(scoredSentence.getValue(), sentenceScore));
                            }

                            // cell.addAttribute("sentenceScoreByMatrixLM", sentenceScoreByMatrixLM);
                            // if startingWithStigmaLeadsToReducedScore or any other which changes the score of sentences
                            // is active, we need to sort the list of sentences again. therefore we use the tokenized sentence list
                            // and look at the first token (as all the other tokens have same score, since belonging to the same sentence)
                            if (startingWithStigmaLeadsToReducedScore) {
                                listOfTokenizedSentences.sort((StringScore[] o1, StringScore[] o2) -> {
                                    return -new Double(o1[0].getScore()).compareTo(o2[0].getScore());
                                });
                                fullSentences.sort((StringScore o1, StringScore o2) -> {
                                    return -new Double(o1.getScore()).compareTo(o2.getScore());
                                });
                            }

                            // at this point we have a list of arrays, 1 array = 1 sentence
                            // the tokens within the array have equal score (that of the sentence)
                            // anyways, add all the tokens to the elements
                            // maybe we should have another structure for sentence, so that the client side can know where a sentence
                            // ends (right now its just a list of tokens)
                            List<StringScore> tmpElements = new ArrayList<>();
                            sentenceCount = 0;
                            for (StringScore[] listOfTokenizedSentence : listOfTokenizedSentences) {
                                for (StringScore scoredToken : listOfTokenizedSentence) {
                                    tmpElements.add(scoredToken);
                                }

                                // check whether we already have enough sentences. note that this should 
                                // be the last operation regarding sentences, because otherwise (e.g. if somehow 
                                // we change the scores of the sentences etc) we could actually have not the best 
                                // sentences in our subset of sentences.
                                if (++sentenceCount >= options.getNumberOfSentences()) {
                                    break;
                                }
                            }
                            elements = tmpElements;

                            break;
                        case SUMMARY_TOKENS:
                        case TOPIC_TOKENS:
                            // token weighting
                            switch (options.getTokenWeighting()) {
                                case NONE:
                                    // set the score for all elements to 1.0
                                    elements = StringScoreUtils.withAllScoresTo(elements, 1.0);
                                    break;
                                case TOKEN_TTFIDF_FROM_SUMMARY_SENTENCES:
                                    // we have that already
                                    break;
                                case POS:
                                    // pos based scoring
                                    elements = posScorer.scoreTokens(StringScore.valueList(elements));
                                    break;
                                default:
                                    throw new IllegalStateException("unknown option: " + options.getTokenWeighting());
                            }

                            // hide stop words
                            if (options.isSummarizationToken_HideStopWords()) {
                                elements = cellStopWordRemover.removeStopwords(cell, elements);
                            }

                            // apply Felix tokens scoring
                            if (options.isSummarizationSentence_FelixScorer()) {
                                for (int i = 0; i < elements.size(); i++) {
                                    StringScore element = elements.get(i);
                                    double tokenScore = element.getScore() + Math.abs(element.getScore()) * felixScorer.computeRelativeScoreOfToken(cell, element.getValue());
                                    elements.set(i, new StringScore(element.getValue(), tokenScore));
                                }
                            }
                            break;
                    }

                    // order
                    switch (options.getElementOrder()) {
                        case NATURAL:
                            // we already have the "natural" / logical order, nothing to do
                            break;
                        case SCORE:
                            // sort by score, greatest first
                            if (options.getSummaryElement() == SummaryElement.SUMMARY_SENTENCES) {
                                // if we have sentences do nothing
                            } else {
                                Collections.sort(elements);
                                Collections.reverse(elements);
                            }
                            break;
                        default:
                            throw new IllegalStateException("unknown");
                    }

                    // add it to cell
                    cell.addAttribute(
                            attrKey,
                            new Cell(
                                    cell.getRowValue(),
                                    cell.getColumnValue(),
                                    elements,
                                    cell.getCountTotalDocsMatchingQuery(),
                                    cell.getTopicProbabilities(),
                                    Arrays.asList(DocumentUtils.getIdsFromDocs(cell.getDocuments().toArray(new Document[0]))),
                                    summaries.getTopDocumentIds(),
                                    cell.getQuery(),
                                    sentenceScoreByMatrixLM,
                                    fullSentences,
                                    summary.getTopSentencesToLightDocIds()
                            )
                    );
                } else {
                    LOG.log(Level.INFO, "information for cell {0} already computed", cell);
                }

                // here we can build the map, with cell id and token whatever
                cells.add((Cell) cell.getAttribute(attrKey));
            }

            LOG.info(cells.toString());

            res.type("application/json");

            return new MatrixInfo(
                    cells,
                    tableManager.getRowDimension().asStringArray(),
                    tableManager.getRowDimension().docCountArray(),
                    tableManager.getColDimension().asStringArray(),
                    tableManager.getColDimension().docCountArray(),
                    analysisProcessor.getAnalysisWorkflow().getTopicExtractor().getTopicsAsMap()
            );
        }, MainController.JSON_TRANSFORMER);
    }
}
