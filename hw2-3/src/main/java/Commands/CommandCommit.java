package Commands;

import Util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandCommit extends AbstractCommand {
    @Override
    public List<String> run(List<String> args) throws MyGitCommandExecException {
        if (args.isEmpty()) {
            throw new MyGitCommandExecException("Command commit needs a commit msg");
        }

        File backupDir = new File(Repository.backupDirName);
        File stageDir = new File(backupDir, Repository.stageDirName);
        if (FileUtils.listFiles(stageDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE).isEmpty()) {
            throw new MyGitCommandExecException("Nothing to commit");
        };

        State state = State.readState();

        Commit curCommit = state.getCurrentCommit();
        Commit newCommit = new Commit();
        newCommit.setUuid(UUID.randomUUID().toString());
        newCommit.setParent(curCommit);
        newCommit.setDate(new Date().toString());
        newCommit.setMessage(String.join(" ", args));
        newCommit.addAllFiles(curCommit.getFiles());

        File newCommitDir = new File(backupDir, newCommit.getUuid());

        if (!newCommitDir.mkdir()) {
            throw new MyGitCommandExecException("Cant create folder for commit");
        }

        try {
            FileUtils.copyDirectory(stageDir, newCommitDir);
            FileUtils.cleanDirectory(stageDir);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyGitCommandExecException("Cant copy files to new commit folder");
        }

        FileUtils.listFiles(newCommitDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE).stream()
                .forEach(file -> {
                    newCommit.addFile(newCommitDir.toPath().relativize(file.toPath()).toString());
                });

        state.addCommit(newCommit);
        state.setCurrentCommit(newCommit);

        String activeBranch = state.getActiveBranch();
        if(activeBranch != null) {
            state.removeBranch(activeBranch);
            state.addBranch(activeBranch, newCommit);
            state.setActiveBranch(activeBranch);
        }

        State.writeState(state);
        return Collections.singletonList("command \'" + getName() + "\' was finished");
    }

    @Override
    public String getName() {
        return "commit";
    }


}
