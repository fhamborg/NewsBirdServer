/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.geo;

import java.util.logging.Logger;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represents a location on earth.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class GeoLocation {

    private static final Logger LOG = Logger.getLogger(GeoLocation.class.getSimpleName());

    private final double latitude;
    private final double longitude;

    public GeoLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
