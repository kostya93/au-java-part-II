package Commands;

import Util.MyGitCommandExecException;
import Util.Repository;
import Util.State;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by kostya on 02.10.2016.
 */
public class CommandClean extends AbstractCommand {
    @Override
    public List<String> run(List<String> args) throws MyGitCommandExecException {
        File rootDir = new File(".");
        File backupDir = new File(Repository.backupDirName);
        File stageDir = new File(backupDir, Repository.stageDirName);

        State state = State.readState();

        IOFileFilter dirFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !(dir.equals(rootDir) && name.equals(Repository.backupDirName));
            }
        };
        Collection<File> allFiles = FileUtils.listFilesAndDirs(rootDir, TrueFileFilter.TRUE, dirFilter);

        List<String> results = new ArrayList<>();

        allFiles.forEach(file -> {
            if (isUntracked(file, state, stageDir, rootDir)) {
                FileUtils.deleteQuietly(file);
                results.add("\'" + file + "\' was deleted");
            }
        });
        return results;
    }

    @Override
    public String getName() {
        return "clean";
    }
}
