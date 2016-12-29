package Util;

import java.util.List;

/**
 * Created by kostya on 02.10.2016.
 */
public interface Repository {
    List<String> execCommand(String[] args);
    String backupDirName = ".mygit";
    String stateFileName = "state";
    String stageDirName = "stage";
}
