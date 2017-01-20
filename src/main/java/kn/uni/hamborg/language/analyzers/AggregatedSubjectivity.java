/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.language.analyzers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This represents Subjectivity aggregated over multiple tokens, e.g., in a
 * sentence, multiple sentences or documents.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class AggregatedSubjectivity implements Serializable {
    
    private static final Logger LOG = Logger.getLogger(AggregatedSubjectivity.class.getSimpleName());

    /**
     * Constructs a single AggregatedSubjectivity.
     */
    public AggregatedSubjectivity() {
        
    }

    /**
     * Constructs a new AggregatedSubjectivity from a list a such. Thereby the
     * counts will be aggregated and minimum and maximum relative frequencies
     * kept.
     *
     * @param listAggregatedSubjectivities
     */
    public AggregatedSubjectivity(List<AggregatedSubjectivity> listAggregatedSubjectivities) {
        for (AggregatedSubjectivity as : listAggregatedSubjectivities) {
            aggregateWith(as);
        }
    }

    /**
     * Incorporates this other AggregatedSubjectivity
     *
     * @param other
     */
    public final void aggregateWith(AggregatedSubjectivity other) {
        countStrongSubjectivity += other.countStrongSubjectivity;
        countWeakSubjectivity += other.countWeakSubjectivity;
        countTokens += other.countTokens;
        
        if (other.getRelativeStrongSubjectivity() >= 0.0 && other.getRelativeStrongSubjectivity() <= 1.0) {
            minRelativeStrongSubjectivity = Math.min(minRelativeStrongSubjectivity, other.getRelativeStrongSubjectivity());
            maxRelativeStrongSubjectivity = Math.max(maxRelativeStrongSubjectivity, other.getRelativeStrongSubjectivity());
        }
        if (other.getRelativeWeakSubjectivity() >= 0.0 && other.getRelativeWeakSubjectivity() <= 1.0) {
            minRelativeWeakSubjectivity = Math.min(minRelativeWeakSubjectivity, other.getRelativeWeakSubjectivity());
            maxRelativeWeakSubjectivity = Math.max(maxRelativeWeakSubjectivity, other.getRelativeWeakSubjectivity());
        }
        
        addInfos(other.getInfos());
    }
    
    private int countStrongSubjectivity = 0;
    private int countWeakSubjectivity = 0;
    private int countTokens = 0;
    private double minRelativeStrongSubjectivity = Double.MAX_VALUE;
    private double maxRelativeStrongSubjectivity = Double.MIN_VALUE;
    private double minRelativeWeakSubjectivity = Double.MAX_VALUE;
    private double maxRelativeWeakSubjectivity = Double.MIN_VALUE;
    private final Set<String> infos = new HashSet<>();
    
    public double getMinRelativeStrongSubjectivity() {
        return minRelativeStrongSubjectivity;
    }
    
    public double getMaxRelativeStrongSubjectivity() {
        return maxRelativeStrongSubjectivity;
    }
    
    public double getMinRelativeWeakSubjectivity() {
        return minRelativeWeakSubjectivity;
    }
    
    public double getMaxRelativeWeakSubjectivity() {
        return maxRelativeWeakSubjectivity;
    }
    
    public void incrementStrongSubjectivity() {
        countStrongSubjectivity++;
    }
    
    public void incrementWeakSubjectivity() {
        countWeakSubjectivity++;
    }
    
    public void incrementTokens() {
        countTokens++;
    }
    
    public int getTotalSubjectivity() {
        return countStrongSubjectivity + countWeakSubjectivity;
    }
    
    public int getCountStrongSubjectivity() {
        return countStrongSubjectivity;
    }
    
    public int getCountWeakSubjectivity() {
        return countWeakSubjectivity;
    }
    
    public int getCountTokens() {
        return countTokens;
    }
    
    public double getRelativeTotalSubjectivity() {
        return (double) getTotalSubjectivity() * 1.0 / getCountTokens();
    }
    
    public double getRelativeStrongSubjectivity() {
        return (double) getCountStrongSubjectivity() * 1.0 / getCountTokens();
    }
    
    public double getRelativeWeakSubjectivity() {
        return (double) getCountWeakSubjectivity() * 1.0 / getCountTokens();
    }
    
    public final void addInfo(String info) {
        infos.add(info);
    }
    
    public final void addInfos(Set<String> infos) {
        this.infos.addAll(infos);
    }
    
    public Set<String> getInfos() {
        return infos;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this) + "\n"
                + "rel weak|strong|total = " + getRelativeWeakSubjectivity() + " | " + getRelativeStrongSubjectivity() + " | " + getRelativeTotalSubjectivity() + "\n"
                + "minmaxweak = " + minRelativeWeakSubjectivity + " | " + maxRelativeWeakSubjectivity + "\n"
                + "minmaxstron= " + minRelativeStrongSubjectivity + " | " + maxRelativeStrongSubjectivity;
    }
    
    public static void main(String[] args) {
        List<String> items = new ArrayList<>();
        items.add("felix");
        items.add("hamborg");
        System.out.println("" + new HashSet<String>(items));
    }
    
}
