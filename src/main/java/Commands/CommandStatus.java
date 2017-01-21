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
 * Created by kostya on 01.10.2016.
 */
public class CommandStatus extends AbstractCommand {
    @Override
    public List<String> run(List<String> args) throws MyGitCommandExecException {
        State state = State.readState();
        List<String> results = new ArrayList<>();
        results.add("active branch='" + state.getActiveBranch() + '\'');
        results.add("");

        File backupDir = new File(Repository.backupDirName);
        File stageDir = new File(backupDir, Repository.stageDirName);
        Collection<File> addedFiles = FileUtils.listFiles(stageDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);

        if (addedFiles.isEmpty()) {
            results.add("No added files");
        } else {
            results.add("Added files:");
            addedFiles.forEach(file -> {
                results.add(stageDir.toPath().relativize(file.toPath()).toString());
            });
        }
        results.add("");
        File rootDir = new File(".");

        IOFileFilter dirFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !(dir.equals(rootDir) && name.equals(Repository.backupDirName));
            }
        };
        Collection<File> allFiles = FileUtils.listFiles(rootDir, TrueFileFilter.TRUE, dirFilter);

        List<String> changedFiles = new ArrayList<>();
        List<String> untrackedFiles = new ArrayList<>();

        allFiles.forEach(file -> {
            if (isChanged(file, state, backupDir, stageDir, rootDir)) {
                changedFiles.add(rootDir.toPath().relativize(file.toPath()).toString());
            }
            if (isUntracked(file, state, stageDir, rootDir)) {
                untrackedFiles.add(rootDir.toPath().relativize(file.toPath()).toString());
            }
        });

        if (!changedFiles.isEmpty()) {
            results.add("Changed file:");
            changedFiles.forEach(results::add);
        } else {
            results.add("No changed files");
        }
        results.add("");

        if (!untrackedFiles.isEmpty()) {
            results.add("Untracked files:");
            untrackedFiles.forEach(results::add);
        } else {
            results.add("No untracked files");
        }
        return results;
    }

    @Override
    public String getName() {
        return "status";
    }
}
