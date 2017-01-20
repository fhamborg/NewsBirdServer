/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.data.emm;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EMMQuote {

    private int who;
    private String verb;
    private String value;

    public int getWho() {
        return who;
    }

    @XmlAttribute(name = "who")
    public void setWho(int who) {
        this.who = who;
    }

    public String getVerb() {
        return verb;
    }

    @XmlAttribute(name = "verb")
    public void setVerb(String verb) {
        this.verb = verb;
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
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
