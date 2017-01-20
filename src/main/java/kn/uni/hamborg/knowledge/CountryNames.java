/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.knowledge;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class CountryNames {

    private static final Logger LOG = Logger.getLogger(CountryNames.class.getSimpleName());

    private static final List<String> countryNamesNormal = new ArrayList<>();
    private static final List<String> countryNamesLowercase = new ArrayList<>();
    public static final String[] countryCodes;

    static {
        countryCodes = Locale.getISOCountries();
        for (String countryCode : countryCodes) {
            Locale obj = new Locale("EN", countryCode);
            countryNamesNormal.add(obj.getDisplayCountry(new Locale("en")));
            countryNamesLowercase.add(obj.getDisplayCountry(new Locale("en")).toLowerCase());
        }
    }
    
  
    public static String getCountryNameFromUCCode(String ucCode) {
        Locale l = new Locale("EN", ucCode);
        return l.getDisplayCountry(new Locale("en"));
    }

    public static boolean isCountry(String countryname) {
        return countryNamesNormal.contains(countryname);
    }

    public static boolean isLowercaseCountry(String countryname) {
        return countryNamesLowercase.contains(countryname);
    }

    public static List<String> getCountryNamesNormal() {
        return countryNamesNormal;
    }

    public static List<String> getCountryNamesLowercase() {
        return countryNamesLowercase;
    }

    public static void main(String[] args) {

    }
}
