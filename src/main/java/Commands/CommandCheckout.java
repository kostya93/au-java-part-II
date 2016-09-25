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
import java.util.List;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandCheckout implements Command {
    @Override
    public void run(List<String> args) throws MyGitCommandExecError {
        if (args.isEmpty()) {
            throw new MyGitCommandExecError("Command checkout needs a branch or commit name");
        }
        State state = State.readState();

        if (args.get(0).equals(state.getCurrentCommit()) || args.get(0).equals(state.getActiveBranch())) {
            throw new MyGitCommandExecError("We already here (" + args.get(0) + ")");
        }

        if(!state.isBranchExist(args.get(0)) && !state.isCommitExist(args.get(0))) {
            throw new MyGitCommandExecError("Branch or commit \"" + args.get(0) + "\" not found");
        }

        boolean isBranch = state.isBranchExist(args.get(0));
        String commitToCheckout;
        String branchToCheckout = null;
        if (isBranch) {
            branchToCheckout = args.get(0);
            commitToCheckout = state.getCommitByBranch(branchToCheckout);
        } else {
            commitToCheckout = args.get(0);
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

        File backupDir = new File(Utils.backupDirName);
        File checkoutToDir = new File(backupDir, commitToCheckout);

        try {
            FileUtils.copyDirectory(checkoutToDir, rootRepoDir);
        } catch (IOException e) {
            String checkoutTo = isBranch ? branchToCheckout : commitToCheckout;
            throw new MyGitCommandExecError("Cant checkout to \"" + checkoutTo + "\"");
        }

        state.setCurrentCommit(commitToCheckout);
        state.setActiveBranch(branchToCheckout);

        State.writeState(state);
    }

    @Override
    public String getName() {
        return "checkout";
    }
}
