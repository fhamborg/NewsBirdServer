/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.topic;

import java.util.logging.Logger;
import kn.uni.hamborg.utils.ObjectScore;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class TopicScore extends ObjectScore {

    private static final Logger LOG = Logger.getLogger(TopicScore.class.getSimpleName());

    private final Topic topic;

    public TopicScore(Topic topic, double score) {
        super(score);
        this.topic = topic;
    }

    public Topic getTopic() {
        return topic;
    }

}
