    /*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.data.emm;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Complete read in.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EMMFullGeo {

    private String name;
    private int id;
    private double geolat;
    private double geolong;
    private int count;
    private String pos;
    private int geoclass;
    /**
     * nullable
     */
    private String iso;
    /**
     * nullable
     */
    private String charpos;
    /**
     * nullable
     */
    private String wordlen;
    /**
     * nullable
     */
    private Integer score;
    private Boolean adjective;
    private String value;

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @XmlAttribute(name = "id")
    public void setId(int id) {
        this.id = id;
    }

    @XmlAttribute(name = "lat")
    public double getGeolat() {
        return geolat;
    }

    public void setGeolat(double geolat) {
        this.geolat = geolat;
    }

    public double getGeolong() {
        return geolong;
    }

    @XmlAttribute(name = "lon")
    public void setGeolong(double geolong) {
        this.geolong = geolong;
    }

    public int getCount() {
        return count;
    }

    @XmlAttribute(name = "count")
    public void setCount(int count) {
        this.count = count;
    }

    public String getPos() {
        return pos;
    }

    @XmlAttribute(name = "pos")
    public void setPos(String pos) {
        this.pos = pos;
    }

    public int getGeoclass() {
        return geoclass;
    }

    @XmlAttribute(name = "class")
    public void setGeoclass(int geoclass) {
        this.geoclass = geoclass;
    }

    public String getIso() {
        return iso;
    }

    @XmlAttribute(name = "iso")
    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getCharpos() {
        return charpos;
    }

    @XmlAttribute(name = "charpos")
    public void setCharpos(String charpos) {
        this.charpos = charpos;
    }

    public String getWordlen() {
        return wordlen;
    }

    @XmlAttribute(name = "wordlen")
    public void setWordlen(String wordlen) {
        this.wordlen = wordlen;
    }

    public int getScore() {
        return score;
    }

    @XmlAttribute(name = "score")
    public void setScore(int score) {
        this.score = score;
    }

    public boolean isAdjective() {
        return adjective;
    }

    @XmlAttribute(name = "adjective")
    public void setAdjective(boolean adjective) {
        this.adjective = adjective;
    }

    public String getValue() {
        return value;
    }

    @XmlValue
    public void setValue(String value) {
        this.value = value;
    }

}
