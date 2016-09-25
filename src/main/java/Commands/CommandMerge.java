package Commands;

import Util.MyGitCommandExecError;
import Util.State;
import Util.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandMerge implements Command {
    @Override
    public void run(List<String> args) throws MyGitCommandExecError {
        if (args.isEmpty()) {
            throw new MyGitCommandExecError("Command merge needs a branch name");
        }

        State state = State.readState();

        String branchToMerge = args.get(0);

        if (branchToMerge.equals(state.getActiveBranch())) {
            throw new MyGitCommandExecError("We already here (" + branchToMerge + ")");
        }

        if(!state.isBranchExist(branchToMerge)) {
            throw new MyGitCommandExecError("Branch \"" + branchToMerge + "\" not found");
        }

        String commitToMerge = state.getCommitByBranch(branchToMerge);

        File backupDir = new File(Utils.backupDirName);
        File dirToMerge = new File(backupDir, commitToMerge);
        File dirCurCommit = new File(backupDir, state.getCurrentCommit());

        for (File file :
                FileUtils.listFiles(dirToMerge, TrueFileFilter.TRUE, TrueFileFilter.TRUE)) {
            List<String> folders = new ArrayList<>();
            File parent = file.getParentFile();
            while (!dirToMerge.equals(parent) && parent != null) {
                folders.add(parent.getName());
                parent = parent.getParentFile();
            }
            File folderInCurCommit = dirCurCommit;
            for (int i = folders.size() - 1; i >= 0; i--) {
                folderInCurCommit = new File(folderInCurCommit, folders.get(i));
            }
            File fileInCurCommit = new File(folderInCurCommit, file.getName());
            try {
                if (fileInCurCommit.exists() && !FileUtils.contentEquals(file, fileInCurCommit)) {
                    throw new MyGitCommandExecError("conflict if file " + file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String mergeCommit = UUID.randomUUID().toString();
        File mergeCommitFolder = new File(backupDir, mergeCommit);

        if (!mergeCommitFolder.mkdir()) {
            throw new MyGitCommandExecError("Cant create folder for commit");
        }

        try {
            FileUtils.copyDirectory(dirToMerge, mergeCommitFolder);
            FileUtils.copyDirectory(dirCurCommit, mergeCommitFolder);
        } catch (IOException e) {
            throw new MyGitCommandExecError("Cant copy files to new commit folder");
        }

        File rootRepoDir = new File(".");

        IOFileFilter dirFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !(dir.equals(rootRepoDir) && name.equals(Utils.backupDirName));
            }
        };

        FileUtils.listFilesAndDirs(rootRepoDir, TrueFileFilter.TRUE, dirFilter)
                .stream().skip(1).forEach(FileUtils::deleteQuietly);

        try {
            FileUtils.copyDirectory(mergeCommitFolder, rootRepoDir);
        } catch (IOException e) {
            throw new MyGitCommandExecError("Cant merge to \"" + branchToMerge + "\"");
        }

        state.addCommit(mergeCommit, "merge branch " + branchToMerge);
        state.addParent(mergeCommit, state.getCurrentCommit());
        state.setCurrentCommit(mergeCommit);
        String activeBranch = state.getActiveBranch();
        if(activeBranch != null) {
            state.removeBranch(activeBranch);
            state.addBranch(activeBranch, mergeCommit);
            state.setActiveBranch(activeBranch);
        }
        state.resetAddedFile();

        State.writeState(state);
    }

    @Override
    public String getName() {
        return "merge";
    }
}
