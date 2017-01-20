/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.config;

import java.io.File;
import kn.uni.hamborg.language.Language;

/**
 * Holds Apache Lucene declarations.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public final class LuceneConfig {

    // November, 2014
    public static final File DATA_DIR_14_11_04_TO_11 = new File(FileConfig.basePathMpLocal, "04-nov-2014");
    public static final File INDEX_DIR_14_11_04_TO_11 = new File(FileConfig.basePathMpLocal, "04-nov-2014-index");
    public static final File INDEX_DIR_14_11_04_TO_11_ALL_LANG_TO_EN = new File(FileConfig.basePathMpLocal, "04-nov-2014-index-all-lang-to-en");

    // full 2014
    public static final File DATA_DIR_2014 = new File(FileConfig.basePathMpLocal, "big");
    public static final File INDEX_DIR_2014_ONLY_EN = new File(FileConfig.basePathMpLocal, "2014-only-en-index");

    // 2015 Grexit
    public static final File DATA_DIR_15_06_TO_07 = new File(FileConfig.basePathMpLocal, "2015-june-july");
    public static final File INDEX_DIR_15_06_TO_07 = new File(FileConfig.basePathMpLocal, "2015-june-july-index");

    public static final File DATA_DIR_2014_WITH_NON_EN = new File(FileConfig.basePathMpLocal, "big");
    public static final File INDEX_DIR_2014_WITH_NON_EN = new File(FileConfig.basePathMpLocal, "2014-with-non-en-index");

    // Facebook, 2011 - April 2015
    public static final File INDEX_DIR_FACEBOOK_MESSAGE_HISTORY = new File(FileConfig.basePathMpLocal, "facebookmsg");

    // Default
    public static final File DATA_DIR_DEFAULT = DATA_DIR_2014;
    public static final File INDEX_DIR_DEFAULT = INDEX_DIR_14_11_04_TO_11; //INDEX_DIR_15_06_TO_07;////INDEX_DIR_14_11_04_TO_11_ALL_LANG_TO_EN;//INDEX_DIR_2014_ONLY_EN;//INDEX_DIR_2014_WITH_NON_EN;

    // Indexing configuration
    public static final short INDEX_NUMBER_OF_THREADS = 8;
    public static final boolean LOAD_INDEX_COMPLETELY_IN_RAM = true;
    public static final Language MAIN_LANGUAGE = Language.EN;

}
