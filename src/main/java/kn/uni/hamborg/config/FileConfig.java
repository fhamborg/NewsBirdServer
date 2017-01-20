/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.config;

import java.io.File;
import org.eclipse.jetty.util.log.Log;

/**
 * Holds file path declarations.
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public final class FileConfig {

    public static final String basePathMpLocal;

    public static final String basePathMpLocalData;

    public static final String basePathMpProject;

    public static final String basePathMpProjectModels;

    public static final String basePathMpProjectData;

    static {
        String linuxPath1 = "/media/felix/extdata/mp-local/";
        String linuxPath2 = "/home/felix/courses/mp_local/";
        String windowsPath = "C:\\priv\\mp-local\\";
        String powerWallPath = "D:\\hamborg\\mp-local\\";

        System.out.println("checking available paths");
        if (new File(linuxPath1).exists()) {
            basePathMpLocal = linuxPath1;
        } else if (new File(linuxPath2).exists()) {
            basePathMpLocal = linuxPath2;
        } else if (new File(windowsPath).exists()) {
            basePathMpLocal = windowsPath;
        } else if (new File(powerWallPath).exists()) {
            basePathMpLocal = powerWallPath;
        } else {
            throw new RuntimeException("no valid path for mp_local has been found on your system");
        }
        System.out.println("setting MPLOCAL = " + basePathMpLocal);

        basePathMpLocalData = basePathMpLocal + "data/";

        basePathMpProject = "./";
        basePathMpProjectModels = basePathMpProject + "models/";
        basePathMpProjectData = basePathMpProject + "data/";
    }
}
