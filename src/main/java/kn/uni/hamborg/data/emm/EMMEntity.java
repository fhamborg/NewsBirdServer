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
 * {@code <emm:entity id="80780" type="o" count="1" pos="3110" name="Cold War">Cold
 * War</emm:entity>}
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class EMMEntity {

    private int id;
    /**
     * this is actually only one single character
     */
    private String type;
    private int count;
    /**
     * this is actually an array of numbers, so we need to convert this later
     */
    private String pos;
    private String name;
    private String value;

    public int getId() {
        return id;
    }

    @XmlAttribute(name = "id")
    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    @XmlAttribute(name = "type")
    public void setType(String type) {
        this.type = type;
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

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "name")
    public void setName(String name) {
        this.name = name;
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
