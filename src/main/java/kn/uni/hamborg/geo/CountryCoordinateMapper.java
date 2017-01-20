/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.geo;

import au.com.bytecode.opencsv.CSVReader;
import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import kn.uni.hamborg.config.GeoConfig;

/**
 * Extracts coordinates for
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CountryCoordinateMapper {

    private static final Logger LOG = Logger.getLogger(CountryCoordinateMapper.class.getSimpleName());

    private static final Map<String, GeoLocation> countryLocations;

    static {
        final long start = new Date().getTime();
        countryLocations = new HashMap<>();
        try {
            try (CSVReader r = new CSVReader(new FileReader(GeoConfig.countryCoordsFile))) {
                r.readNext();

                String[] line = r.readNext();
                while (line != null) {
                    countryLocations.put(line[0], new GeoLocation(Double.valueOf(line[1]), Double.valueOf(line[2])));
                    line = r.readNext();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CountryCoordinateMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        LOG.log(Level.INFO, "parsed " + countryLocations.size() + " Country Coordinates in {0}ms", (new Date().getTime() - start));
    }

    /**
     * Returns the geo location for the country. see
     * http://dev.maxmind.com/geoip/legacy/codes/country_latlon/
     *
     * See also here for maps and stuff.
     * http://download.geonames.org/export/dump/
     *
     * @param cc
     * @return
     */
    public static GeoLocation getGeoLocationByCountryCode(String cc) {
        return countryLocations.get(cc);
    }

    public static void main(String[] args) {
        System.out.println(new Gson().toJson(countryLocations));
        System.out.println(countryLocations.get("DE").toString());
        System.out.println(countryLocations.keySet());
        System.out.println(countryLocations.toString());
    }
}
