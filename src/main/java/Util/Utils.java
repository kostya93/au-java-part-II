package Util;

import java.io.File;

/**
 * Created by kostya on 25.09.2016.
 */
public class Utils {
    public final static String backupDirName = ".mygit";
    public final static String stateFileName = "state";

    public static boolean isRepositoryExist() {
        File backupDir = new File(backupDirName);
        return (
                backupDir.exists()
                && backupDir.isDirectory()
                && backupDir.canRead()
                && backupDir.canWrite()
        );
    }

    public static void printHelp() {
        System.out.println("help");
    }
}
