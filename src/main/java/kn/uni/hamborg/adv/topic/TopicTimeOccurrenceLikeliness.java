/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.topic;

import java.io.IOException;
import java.util.logging.Logger;
import kn.uni.hamborg.adv.AnalysisConfiguration;
import kn.uni.hamborg.utils.QueryUtils;
import kn.uni.hamborg.web.query.QueryCommand;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TopicTimeOccurrenceLikeliness {

    private static final Logger LOG = Logger.getLogger(TopicTimeOccurrenceLikeliness.class.getSimpleName());

    private static final int NUMBER_OF_WEEKS_BEFORE = 8;

    private final QueryCommand queryCommand;
    private final IndexSearcher indexSearcher;
    private final TopicExtractor topicExtractor;
    protected final QueryParser queryParser;

    public TopicTimeOccurrenceLikeliness(TopicExtractor topicExtractor, QueryCommand queryCommand, IndexSearcher indexSearcher, QueryParser queryParser) {
        this.queryCommand = queryCommand;
        this.indexSearcher = indexSearcher;
        this.queryParser = queryParser;
        this.topicExtractor = topicExtractor;
    }

    /**
     * Compute likeliness of the {@link Topic}s from the current matrix.
     */
    public void computeTopicsLikeliness() {
        for (Topic topic : topicExtractor.getTopicsAsMap().values()) {
            double likeliness = getTopicLikeliness(topic);
            topic.addAttribute("timeOccurrenceLikeliness", likeliness);
        }
    }

    /**
     * Measures the likeliness of a topic with respect to the occurence of that
     * topic a few weeks before the start date of the matrix. The idea is that
     * topics which are specific only occur at few dates and are otherwise very
     * uncommon (to non existent).
     *
     * The more the frequency from that ground-date-interval differs from the
     * matrix-date-interval the more unexpected this is and maybe the more
     * interesting (at least date specific) this topic is.
     *
     * However, topics such as "putin russia russian ..." might have a high
     * frequency on many dates so this idea might not work always.
     *
     *
     * @param topic
     * @return {@code 0} if the the frequency in the ground date interval is the
     * same as in the matrix date interval<br> {@code >0} the topic is in the
     * matrix more common than in the ground date interval<br> {@code <0} the
     * topic is in the matrix less common than in the ground date interval
     */
    private double getTopicLikeliness(Topic topic) {
        try {
            final Interval matrixInterval = queryCommand.getFromTo();
            final DateTime matrixStartDate = matrixInterval.getStart();

            // get ground interval
            final DateTime groundIntervalStartDate = matrixStartDate.minusWeeks(NUMBER_OF_WEEKS_BEFORE);
            final Interval groundInterval = new Interval(groundIntervalStartDate, matrixInterval.toPeriod());

            // userFilter from queryCommand could be used here as well, but maybe it is better
            // to really get the overall data without any restrictions but the date
            LOG.info("creating ground query, without userFilter!");
            final Query groundBaseQuery = QueryCommand.buildDateQuery(groundInterval);
            final Query matrixBaseQuery = QueryCommand.buildDateQuery(queryCommand.getFromTo());

            final Query topicQuery = TopicExtractor.getQueryForTopic(topic, queryParser, AnalysisConfiguration.topicQueryForceAllTopTermsIncluded);

            // this is the same as in the current matrix but a few weeks before that to find out whether the topic was already common / incommon then.
            final Query groundTopicQuery = QueryUtils.addQueryToQuery(groundBaseQuery, topicQuery);
            // same as in a the current matrix (same date filters, but without userFilter restriction)
            final Query matrixTopicQuery = QueryUtils.addQueryToQuery(matrixBaseQuery, topicQuery);

            System.out.println(groundTopicQuery);
            System.out.println(matrixTopicQuery);
            double groundtopicHits = indexSearcher.search(groundTopicQuery, Integer.MAX_VALUE).totalHits;
            double matrixTopicHits = indexSearcher.search(matrixTopicQuery, Integer.MAX_VALUE).totalHits;
            double max = 100000;

            if (matrixTopicHits == groundtopicHits) {
                return 0.0;
            } else if (matrixTopicHits < groundtopicHits) {
                return -Math.min(groundtopicHits / matrixTopicHits, max);
            } else {
                return Math.min(matrixTopicHits / groundtopicHits, max);
            }
        } catch (IOException | ParseException ex) {
            throw new RuntimeException(ex);
        }
    }
}
