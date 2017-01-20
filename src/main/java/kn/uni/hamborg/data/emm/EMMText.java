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
 * complete read in.
 *
 * @author felix
 */
public class EMMText {

    private int wordCount;
    private String value;

    public int getWordCount() {
        return wordCount;
    }

    @XmlAttribute(name = "wordCount")
    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public String getValue() {
        return value;
    }

    @XmlValue
    public void setValue(String value) {
       // if( value != null){
            this.value = value.trim();
       // }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
