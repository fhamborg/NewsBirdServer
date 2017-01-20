package kn.uni.hamborg.web.images;

/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import kn.uni.hamborg.config.ImageConfig;
import kn.uni.hamborg.config.PublicConfig;

/**
 * Searches online for images given a query and downloads them. Also stores them
 * offline, so that if the same query is requested no online search will be
 * performed.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ImageSearch {

    private static final Logger LOG = Logger.getLogger(ImageSearch.class.getSimpleName());

    private static final String KEY = "AIzaSyARbgCEI8d5TLa5KVHvGWIsYdIGzM9nNQA";
    private static final int NUMBER_OF_RESULTS = 1;
    // Yields in only getting icons.
    private static final String ADDITIONAL_PARAMS = "&imgSize=icon";

    /**
     * Sends a query to Google Custom Search for images
     *
     * @param query Regular query string, will be URL encoded internally
     * @return
     */
    public static String sendQueryAndDownloadFile(String query) {
        if (getFileForQuery(query).exists()) {
            LOG.log(Level.INFO, "image exists for ''{0}''", query);
            return getPublicFileForQuery(query);
        }

        LOG.log(Level.INFO, "need to get image for ''{0}''", query);
        try {
            URL url = new URL(
                    "https://www.googleapis.com/customsearch/v1?key=" + KEY
                    + "&cx=013036536707430787589:_pqjad5hr1a&"
                    + "q=" + URLEncoder.encode(query)
                    + "&alt=json"
                    + "&num=" + NUMBER_OF_RESULTS
                    + ADDITIONAL_PARAMS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                final JsonObject result = new JsonParser().parse(new InputStreamReader(
                        (conn.getInputStream()))).getAsJsonObject();
                conn.disconnect();

                // after the results are downloaded and converted to JsonObject and we have closed the connection, we can start
                // parsing all of the results
                final JsonArray items = result.getAsJsonArray("items");
                // currently only get one picture
                final JsonObject item = items.get(0).getAsJsonObject();
                // get the image url
                final String imgUrl = item.getAsJsonObject("pagemap").getAsJsonArray("cse_image").get(0).getAsJsonObject().get("src").getAsString();

                // download the image
                saveImage(imgUrl, getFileForQuery(query));

                return getPublicFileForQuery(query);
            }

            // in case of no success, we get here if the limit is reached
            final JsonObject result = new JsonParser().parse(new InputStreamReader(
                    (conn.getErrorStream()))).getAsJsonObject();
            conn.disconnect();
            LOG.log(Level.SEVERE, "q=''{0}'': {1}", new Object[]{query, result.toString()});
            return null;
        } catch (NullPointerException ne) {
            // we get here if no image was available, google didnt return any image
            // ne.printStackTrace();
            return getPublicFileFromFile(new File(ImageConfig.onlineQueryPath, "FFFFFF-0.png"));
        } catch (IOException | JsonIOException | JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(sendQueryAndDownloadFile("putin"));
    }

    /**
     * Generates a file based on the given query. This file might not exist
     * (yet). We use base64 encoded query to be sure that no invalid character
     * (OS depending) breaks writing to disk.
     *
     * @param query
     *
     * @return
     */
    protected static File getFileForQuery(String query) {
        String base64 = getFilenameForQuery(query);
        return new File(ImageConfig.onlineQueryPath, base64);
    }

    protected static String getFilenameForQuery(String query) {
        return DatatypeConverter.printBase64Binary(query.getBytes());
    }

    protected static String getPublicFileForQuery(String query) {
        return getPublicFileFromFile(getFileForQuery(query));
    }

    protected static String getPublicFileFromFile(File file) {
        return file.getPath().replaceAll("\\\\", "/").substring(PublicConfig.publicBase.getPath().length());
    }

    /**
     * Downloads a (image) file and saves it
     *
     * @param imageUrl
     * @param destinationFile
     * @throws IOException
     */
    protected static void saveImage(String imageUrl, File destinationFile) throws IOException {
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();

        LOG.log(Level.INFO, "saved image {0}", imageUrl);
    }
}
