/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kn.uni.hamborg.data.emm;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author felix
 */
public class EMMSource {

    private String url;
    private String country;
    private int rank;
    private String value;

    public String getUrl() {
        return url;
    }

    @XmlAttribute(name = "url")
    public void setUrl(String url) {
        this.url = url;
    }

    public String getCountry() {
        return country;
    }

    @XmlAttribute(name = "country")
    public void setCountry(String country) {
        this.country = country;
    }

    public int getRank() {
        return rank;
    }

    @XmlAttribute(name = "rank")
    public void setRank(int rank) {
        this.rank = rank;
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
