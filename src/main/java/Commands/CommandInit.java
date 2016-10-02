package Commands;

import Util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandInit extends AbstractCommand {
    @Override
    public List<String> run(List<String> args) throws MyGitCommandExecException {
        File backupDir = new File(Repository.backupDirName);
        if (!backupDir.exists()) {
            if (!backupDir.mkdir()) {
                throw new MyGitCommandExecException("Cant create backup folder");
            }
        } else {
            throw new MyGitCommandExecException("Repository already exist");
        }

        File stateFile = new File(backupDir, Repository.stateFileName);
        try {
            if (!stateFile.createNewFile()) {
                throw new MyGitCommandExecException("Cant create state file");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        File stageDir = new File(backupDir, Repository.stageDirName);
        if (!stageDir.mkdir()) {
            throw new MyGitCommandExecException("Cant create stage folder");
        }

        Commit commit = new Commit();
        commit.setUuid(UUID.randomUUID().toString());
        commit.setMessage("init commit");
        commit.setDate(new Date().toString());
        commit.setFiles(new HashSet<>());
        commit.setParent(null);

        File commitDir = new File(backupDir, commit.getUuid());
        if(!commitDir.mkdir()) {
            throw new MyGitCommandExecException("Cant create commit dir");
        }

        State newState = new State();
        newState.setActiveBranch("master");
        newState.addBranch(newState.getActiveBranch(), commit);
        newState.addCommit(commit);
        newState.setCurrentCommit(commit);

        State.writeState(newState);
        return Collections.singletonList("command \'" + getName() + "\' was finished");
    }

    @Override
    public String getName() {
        return "init";
    }
}
