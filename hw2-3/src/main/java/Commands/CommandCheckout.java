package Commands;

import Util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandCheckout extends AbstractCommand {
    @Override
    public List<String> run(List<String> args) throws MyGitCommandExecException {
        if (args.isEmpty()) {
            throw new MyGitCommandExecException("Command checkout needs a branch or commit name");
        }

        State state = State.readState();

        if (args.get(0).equals(state.getCurrentCommit().getUuid()) || args.get(0).equals(state.getActiveBranch())) {
            throw new MyGitCommandExecException("We already here (" + args.get(0) + ")");
        }

        if(!state.isBranchExist(args.get(0)) && !state.isCommitExistByUuid(args.get(0))) {
            throw new MyGitCommandExecException("Branch or commit \"" + args.get(0) + "\" not found");
        }

        boolean isBranch = state.isBranchExist(args.get(0));
        String uuidToCheckout;
        String branchToCheckout = null;
        if (isBranch) {
            branchToCheckout = args.get(0);
            uuidToCheckout = state.getCommitByBranch(branchToCheckout).getUuid();
        } else {
            uuidToCheckout = args.get(0);
        }

        File rootDir = new File(".");
        File backupDir = new File(Repository.backupDirName);

        RemoveTrackedFilesFromRoot(state);

        Commit commit = state.getCommitByUUID(uuidToCheckout);
        Set<String> copiedFiles = new HashSet<>();

        while (commit != null) {
            if (commit.getFiles().equals(copiedFiles)) {
                break;
            }
            File commitDir = new File(backupDir, commit.getUuid());
            FileUtils.listFiles(commitDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE)
                    .stream()
                    .filter(file -> !copiedFiles.contains(commitDir.toPath().relativize(file.toPath()).toString()))
                    .forEach(file -> {
                        File parentInCommit = file.getParentFile();
                        try {
                            if (parentInCommit != null && !parentInCommit.equals(commitDir)) {
                                File parentInRoot = new File(rootDir,commitDir.toPath().relativize(parentInCommit.toPath()).toString());
                                parentInRoot.mkdirs();
                                FileUtils.copyFileToDirectory(file, parentInRoot);
                            } else {
                                FileUtils.copyFileToDirectory(file, rootDir);
                            }
                            copiedFiles.add(commitDir.toPath().relativize(file.toPath()).toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            commit = commit.getParent();
        }

        state.setCurrentCommit(state.getCommitByUUID(uuidToCheckout));
        state.setActiveBranch(branchToCheckout);

        State.writeState(state);
        return Collections.singletonList("command \'" + getName() + "\' was finished");
    }

    @Override
    public String getName() {
        return "checkout";
    }
}
