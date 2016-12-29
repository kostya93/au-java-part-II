package Commands;

import Util.Commit;
import Util.Repository;
import Util.State;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;

/**
 * Created by kostya on 02.10.2016.
 */
public abstract class AbstractCommand implements Command {
    static boolean isUntracked(File file, State state, File stageDir, File rootDir) {

        boolean inStage = FileUtils.listFilesAndDirs(stageDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE)
                .stream()
                .map(f -> stageDir.toPath().relativize(f.toPath()))
                .anyMatch(s -> s.equals(rootDir.toPath().relativize(file.toPath())));

        if (inStage) {
            return false;
        }

        Commit curCommit = state.getCurrentCommit();

        if (curCommit.getFiles().contains(rootDir.toPath().relativize(file.toPath()).toString())) {
            return false;
        }

        if (file.isFile()) {
            return true;
        }

        if (file.isDirectory()) {
            for(String fileName: curCommit.getFiles()) {
                File curFile = new File(rootDir, fileName);
                File parent = curFile.getParentFile();
                if (file.equals(parent)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    static boolean isChanged(File file, State state, File backupDir, File stageDir, File rootDir) {
        File fileInStageDir = new File(stageDir, file.getPath());
        if (fileInStageDir.exists()) {
            try {
                return !FileUtils.contentEquals(file, fileInStageDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Commit curCommit = state.getCurrentCommit();
        if (!curCommit.getFiles().contains(rootDir.toPath().relativize(file.toPath()).toString())) {
            return false;
        }
        while (true){
            if (curCommit == null) {
                break;
            }

            File curCommitDir = new File(backupDir, curCommit.getUuid());
            File fileInCurCommitDir = new File(curCommitDir, file.getPath());

            if (fileInCurCommitDir.exists()) {
                try {
                    return !FileUtils.contentEquals(file, fileInCurCommitDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            curCommit = curCommit.getParent();
        }
        return false;
    }

    public static void RemoveTrackedFilesFromRoot(State state) {
        File rootDir = new File(".");
        File backupDir = new File(rootDir, Repository.backupDirName);
        File stageDir = new File(backupDir, Repository.stageDirName);
        IOFileFilter dirFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !(dir.equals(rootDir) && name.equals(Repository.backupDirName));
            }
        };

        FileUtils.listFilesAndDirs(rootDir, TrueFileFilter.TRUE, dirFilter)
                .stream()
                .skip(1)
                .filter(file -> !isUntracked(file, state, stageDir, rootDir))
                .forEach(FileUtils::deleteQuietly);
    }
}
