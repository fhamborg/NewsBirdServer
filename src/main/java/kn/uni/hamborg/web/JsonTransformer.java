/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.web;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.logging.Logger;
import spark.ResponseTransformer;

/**
 * This renders any given (response) object into a JSON string.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class JsonTransformer implements ResponseTransformer {

    private static final Logger LOG = Logger.getLogger(JsonTransformer.class.getSimpleName());

    private final Gson gson = Converters.registerDateTime(new GsonBuilder()).create();

    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }

    public Gson getGson() {
        return gson;
    }

}
