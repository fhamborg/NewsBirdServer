/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.gui.era;

import java.util.logging.Logger;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ArticleSelectionChangedEvent {

    private static final Logger LOG = Logger.getLogger(ArticleSelectionChangedEvent.class.getSimpleName());

    private final String firstArticleContent;
    private final int articleCount;

    public ArticleSelectionChangedEvent(String firstArticleContent, int articleCount) {
        this.firstArticleContent = firstArticleContent;
        this.articleCount = articleCount;
    }

    public String getFirstArticleContent() {
        return firstArticleContent;
    }

    public int getArticleCount() {
        return articleCount;
    }

}
