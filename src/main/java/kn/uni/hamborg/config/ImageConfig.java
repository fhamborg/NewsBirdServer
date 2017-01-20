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
public class ImageConfig {

    private static final Logger LOG = Logger.getLogger(ImageConfig.class.getSimpleName());

    private static final File base = new File(PublicConfig.publicBase, "images");
    public static final File onlineQueryPath = new File(base, "onlinequery");
}
