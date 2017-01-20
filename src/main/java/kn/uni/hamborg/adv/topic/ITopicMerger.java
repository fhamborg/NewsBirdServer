/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.adv.topic;

/**
 * Defines the interface for a topic merger.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public interface ITopicMerger {

    /**
     * Merges those topics to one, which are very similar.
     *
     *
     */
    void mergeTopics();
}
