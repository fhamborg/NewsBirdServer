/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web;

import java.util.logging.Logger;
import org.joda.time.DateTime;

/**
 * Represents an article.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class Article {

    private static final Logger LOG = Logger.getLogger(Article.class.getSimpleName());

    private final String title;
    private final String content;
    private final String description;
    private final String url;
    private final String channelGuid;
    private final DateTime pubDate;

    public Article(String title, String content, String description, String url, String channelGuid, DateTime pubDate) {
        this.title = title;
        this.content = content;
        this.description = description;
        this.url = url;
        this.channelGuid = channelGuid;
        this.pubDate = pubDate;
    }

}
