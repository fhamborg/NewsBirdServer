/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.data.emm;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;

/**
 *
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EMMItem {

    private int positionInChannel;

    private String id;
    private String title;
    /**
     * nullable
     */
    private String titleEn;
    /**
     * nullable
     */
    private String titleLang;
    private String link;
    private String description;
    /**
     * nullable
     */
    private String descriptionEn;
    /**
     * nullable
     */
    private String translate;
    /**
     * nullable
     */
    private String translateLang;
    private String contentType;
    private DateTime pubDate;

    private EMMSource source;

    private String language;
    private String guid;
    private String favicon;
    private List<EMMCategory> categories;
    private List<EMMEntity> entities;
    private List<EMMGeoRss> georss;
    private List<EMMFullGeo> fullGeos;
    /**
     * nullable
     */
    private List<EMMQuote> quotes;
    /**
     * nullable
     */
    private EMMText textEn;
    private EMMText text;
    //private int textWordCount;
    private int tonality;

    public String getId() {
        return id;
    }

    @XmlAttribute(namespace = "http://emm.jrc.it")
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    @XmlElement(namespace = "", name = "title")
    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleEn() {
        return titleEn;
    }

    @XmlElement(namespace = "http://emm.jrc.it", name = "title")
    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getTitleLang() {
        return titleLang;
    }

    public void setTitleLang(String titleLang) {
        this.titleLang = titleLang;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    @XmlElement(namespace = "", name = "description")
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    @XmlElement(namespace = "http://emm.jrc.it", name = "description")
    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    public String getTranslateLang() {
        return translateLang;
    }

    public void setTranslateLang(String translateLang) {
        this.translateLang = translateLang;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public DateTime getPubDate() {
        return new DateTime(pubDate);
    }

    @XmlElement(name = "pubDate")
    @XmlJavaTypeAdapter(EMMRssParser.DateAdapterItem.class)
    public void setPubDate(DateTime pubDate) {
        this.pubDate = pubDate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        if (language != null) {
            language = language.toUpperCase();
        }
        this.language = language;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public List<EMMCategory> getCategories() {
        return categories;
    }

    @XmlElement(name = "category")
    public void setCategories(List<EMMCategory> categories) {
        this.categories = categories;
    }

    public List<EMMEntity> getEntities() {
        return entities;
    }

    @XmlElement(name = "entity")
    public void setEntities(List<EMMEntity> entities) {
        this.entities = entities;
    }

    public List<EMMGeoRss> getGeorss() {
        return georss;
    }

    @XmlElement(name = "georss")
    public void setGeorss(List<EMMGeoRss> georss) {
        this.georss = georss;
    }

    public List<EMMFullGeo> getFullGeos() {
        return fullGeos;
    }

    @XmlElement(name = "fullgeo")
    public void setFullGeos(List<EMMFullGeo> fullGeos) {
        this.fullGeos = fullGeos;
    }

    public List<EMMQuote> getQuotes() {
        return quotes;
    }

    @XmlElement(name = "quote")
    public void setQuotes(List<EMMQuote> quotes) {
        this.quotes = quotes;
    }

    public EMMText getTextEn() {
        return textEn;
    }

    @XmlElement(namespace = "http://emm.jrc.it", name = "text-en")
    public void setTextEn(EMMText textEn) {
        this.textEn = textEn;
    }

    public EMMText getText() {
        return text;
    }

    @XmlElement(namespace = "http://emm.jrc.it", name = "text")
    public void setText(EMMText text) {
        this.text = text;
    }

    /*   public int getTextWordCount() {
     return textWordCount;
     }

     public void setTextWordCount(int textWordCount) {
     this.textWordCount = textWordCount;
     }*/
    public int getTonality() {
        return tonality;
    }

    public void setTonality(int tonality) {
        this.tonality = tonality;
    }

    public EMMSource getSource() {
        return source;
    }

    public void setSource(EMMSource source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public int getPositionInChannel() {
        return positionInChannel;
    }

    public void setPositionInChannel(int positionInChannel) {
        this.positionInChannel = positionInChannel;
    }

}
