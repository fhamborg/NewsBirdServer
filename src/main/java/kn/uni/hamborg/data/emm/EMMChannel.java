/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.data.emm;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EMMChannel {

    private String filename;
    private String pathname;

    private String title;
    private String language;
    private String guid;
    private DateTime pubDate;
    private List<EMMItem> items;

    public String getTitle() {
        return title;
    }

    @XmlElement
    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    @XmlElement
    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGuid() {
        return guid;
    }

    @XmlElement
    public void setGuid(String guid) {
        this.guid = guid;
    }

    public DateTime getPubDate() {
        return pubDate;
    }

    @XmlElement(name = "pubDate")
    @XmlJavaTypeAdapter(EMMRssParser.DateAdapterChannel.class)
    public void setPubDate(DateTime pubDate) {
        this.pubDate = pubDate;
    }

    public List<EMMItem> getItems() {
        return items;
    }

    @XmlElement(name = "item")
    public void setItems(List<EMMItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }

    public String getFilename() {
        return filename;
    }

    public String getPathname() {
        return pathname;
    }

}
