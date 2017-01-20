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
public class EMMCategory {

    /**
     * nullable
     */
    private Integer rank;
    /**
     * nullable
     */
    private Integer score;
    private String trigger;
    private String value;

    public int getRank() {
        return rank;
    }

    @XmlAttribute(namespace = "http://emm.jrc.it", name = "rank")
    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getScore() {
        return score;
    }

    @XmlAttribute(namespace = "http://emm.jrc.it", name = "score")
    public void setScore(int score) {
        this.score = score;
    }

    public String getTrigger() {
        return trigger;
    }

    @XmlAttribute(namespace = "http://emm.jrc.it", name = "trigger")
    public void setTrigger(String trigger) {
        this.trigger = trigger;
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
