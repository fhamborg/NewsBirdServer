/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represents one line from the file extracted in
 * {@link MPQASubjectivityExtractor}.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class SubjectivityWord implements Serializable {

    private Subjectivity.Type type;
    private Subjectivity.Polarity polarity;
    private String word;
    private String pos;
    private SimplePos simplePos;

    private SubjectivityWord() {
        // just for deserialization
    }

    public SubjectivityWord(Subjectivity.Type type, Subjectivity.Polarity polarity,
            String word, String pos, SimplePos simplePos) {
        this.type = type;
        this.polarity = polarity;
        this.word = word;
        this.pos = pos;
        this.simplePos = simplePos;
    }

    public Subjectivity.Type getType() {
        return type;
    }

    public Subjectivity.Polarity getPolarity() {
        return polarity;
    }

    public String getWord() {
        return word;
    }

    public String getPos() {
        return pos;
    }

    public SimplePos getSimplePos() {
        return simplePos;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
