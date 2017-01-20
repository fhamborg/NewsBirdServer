/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.data.light;

import java.io.Serializable;
import kn.uni.hamborg.lucene.field.VecTextField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.joda.time.DateTime;

/**
 * This class presents a light document, i.e., containing only that dimensions
 * which we need at some point later.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class LightDoc implements Serializable {

    /**
     * Definitions for field names.
     */
    public static final String CONTENT = "content";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String CHANNEL_GUID = "channelGuid";
    public static final String CHANNEL_LANGUAGE = "channelLanguage";
    public static final String PUB_COUNTRY = "pubCountry";
    public static final String PUB_DATE = "pubDate";
    public static final String PUB_URL = "pubUrl";
    public static final String ID = "id";
    public static final String CHANNEL_FILE = "channelFile";
    public static final String CHANNEL_POSITION = "channelPosition";
    public static final String RECIPIENTS = "recipients";

    /**
     * Stemmed field names.
     */
    public static final String SUFFIX_STEMMED = "Stemmed";
    public static final String TITLE_STEMMED = TITLE + SUFFIX_STEMMED;
    public static final String CONTENT_STEMMED = CONTENT + SUFFIX_STEMMED;
    public static final String DESCRIPTION_STEMMED = DESCRIPTION + SUFFIX_STEMMED;

    /**
     * Members.
     */
    private String id;
    private String content;
    private String description;
    private String title;
    private String channelGuid;
    private String channelLanguage;
    private int positionInChannel;
    private String channelFile;
    private String link;
    private DateTime pubDate;
    private String pubLand;
    private String[] recipients;

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getChannelGuid() {
        return channelGuid;
    }

    public String getChannelLanguage() {
        return channelLanguage;
    }

    public String getLink() {
        return link;
    }

    public DateTime getPubDate() {
        return pubDate;
    }

    public String getPubLand() {
        return pubLand;
    }

    public String[] getRecipients() {
        return recipients;
    }

    public LightDoc(String id, String text, String title, String description, String channelGuid,
            String channelLanguage, String link, DateTime pubDate, String pubLand,
            String channelPath, int positionInChannel) {
        this(id, text, title, description, channelGuid, channelLanguage, link, pubDate, pubLand, new String[0], channelPath, positionInChannel);
    }

    public LightDoc(String id, String text, String title, String description, String channelGuid,
            String channelLanguage, String link, DateTime pubDate, String pubLand,
            String[] recipients, String channelPath, int positionInChannel) {
        this.id = id;
        this.content = text;
        this.title = title;
        this.description = description;
        this.channelGuid = channelGuid;
        this.channelLanguage = channelLanguage;
        this.channelFile = channelPath;
        this.positionInChannel = positionInChannel;
        this.link = link;
        this.recipients = recipients;
        this.pubDate = pubDate;
        this.pubLand = pubLand;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof LightDoc) {
            LightDoc other = (LightDoc) obj;

            return other.id.equals(id);

            /*  return other.content.equals(content)
             && other.title.equals(title)
             && other.channelLanguage.equals(channelLanguage)
             && other.channelGuid.equals(channelGuid)
             && other.pubDate.equals(pubDate)
             && other.link.equals(link)
             && other.pubLand.equals(pubLand);*/
        }

        return false;
    }

    public int getPositionInChannel() {
        return positionInChannel;
    }

    public void setPositionInChannel(int positionInChannel) {
        this.positionInChannel = positionInChannel;
    }

    public String getChannelFile() {
        return channelFile;
    }

    public void setChannelFile(String channelFile) {
        this.channelFile = channelFile;
    }

    /**
     * Creates a new lucene document from this LightDoc instance. Each time this
     * function is invoked, a new lucene document is created.
     *
     * @return
     */
    public Document asLuceneDocument() {
        final Document doc = new Document();

        // store the id
        doc.add(new StringField(ID, id, Field.Store.YES));

        // we store actually all values completely, might be a problem for very large document collections
        doc.add(new VecTextField(CONTENT, content, Field.Store.YES));
        doc.add(new VecTextField(TITLE, title, Field.Store.YES));
        doc.add(new VecTextField(DESCRIPTION, description, Field.Store.YES));

        doc.add(new StringField(CHANNEL_GUID, channelGuid, Field.Store.YES));
        doc.add(new StringField(CHANNEL_LANGUAGE, channelLanguage, Field.Store.YES));

        doc.add(new StringField(PUB_COUNTRY, pubLand, Field.Store.YES));
        doc.add(new LongField(PUB_DATE, pubDate.getMillis(), Field.Store.YES));
        doc.add(new StringField(PUB_URL, link, Field.Store.YES));

        // additional stuff
        for (String recipient : recipients) {
            doc.add(new StringField(RECIPIENTS, recipient, Field.Store.YES));
        }

        // stemmed fields (we don't need to store the original content twice, as it is already stored in the CONTENT and TITLE field)
        // however, we also store this in order to allow reprocessing / analysis of these fields, e.g., for summarization
        doc.add(new VecTextField(CONTENT_STEMMED, content, Field.Store.YES));
        doc.add(new VecTextField(TITLE_STEMMED, title, Field.Store.YES));
        doc.add(new VecTextField(DESCRIPTION_STEMMED, description, Field.Store.YES));

        // additional information to get from this Lucene document back to the original file
        doc.add(new StringField(CHANNEL_FILE, channelFile, Field.Store.YES));
        doc.add(new IntField(CHANNEL_POSITION, positionInChannel, Field.Store.YES));

        return doc;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
