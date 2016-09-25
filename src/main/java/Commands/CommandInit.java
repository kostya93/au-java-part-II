package Commands;

import Util.MyGitCommandExecError;
import Util.State;
import Util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandInit implements Command {
    @Override
    public void run(List<String> args) throws MyGitCommandExecError {
        File backupDir = new File(Utils.backupDirName);
        if (!backupDir.exists()) {
            if (!backupDir.mkdir()) {
                throw new MyGitCommandExecError("Cant create backup folder");
            }
        } else {
            throw new MyGitCommandExecError("Repository already exist");
        }

        File stateFile = new File(backupDir, Utils.stateFileName);
        try {
            if (!stateFile.createNewFile()) {
                throw new MyGitCommandExecError("Cant create state file");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        String initCommit = UUID.randomUUID().toString();

        File commitDir = new File(backupDir, initCommit);
        if(!commitDir.mkdir()) {
            throw new MyGitCommandExecError("Cant create commit dir");
        }


        State newState = new State();
        newState.setActiveBranch("master");
        newState.addBranch(newState.getActiveBranch(), initCommit);
        newState.addCommit(initCommit, "init commit");
        newState.setCurrentCommit(initCommit);
        newState.addParent(initCommit, null);

        State.writeState(newState);
    }

    @Override
    public String getName() {
        return "init";
    }
}
