/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.data.emm;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Complete read in.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EMMGeoRss {

    private String name;
    private int id;
    private double geoLat;
    private double geoLong;
    private int geoclass;
    private String iso;
    private String charpos;
    private String wordlen;
    private int score;
    private String value;

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "name")
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

    public double getGeoLat() {
        return geoLat;
    }

    @XmlAttribute(name = "lat")
    public void setGeoLat(double geoLat) {
        this.geoLat = geoLat;
    }

    public double getGeoLong() {
        return geoLong;
    }

    @XmlAttribute(name = "lon")
    public void setGeoLong(double geoLong) {
        this.geoLong = geoLong;
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

    public String getValue() {
        return value;
    }

    @XmlValue
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
