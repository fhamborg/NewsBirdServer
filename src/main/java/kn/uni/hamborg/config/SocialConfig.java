/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.config;

import java.io.File;
import java.util.logging.Logger;

/**
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class SocialConfig {

    private static final Logger LOG = Logger.getLogger(SocialConfig.class.getSimpleName());

    private static final File baselocaldata = new File(FileConfig.basePathMpLocalData, "social");

    public static final File facebookMessageHistoryHtmlFile = new File(baselocaldata, "messages.htm");

}
