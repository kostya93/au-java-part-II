package Commands;

import Util.MyGitCommandExecError;
import Util.State;
import Util.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandCommit implements Command {
    @Override
    public void run(List<String> args) throws MyGitCommandExecError {
        if (args.isEmpty()) {
            throw new MyGitCommandExecError("Command commit needs a commit msg");
        }

        State state = State.readState();

        if (state.getFiles().isEmpty()){
            throw new MyGitCommandExecError("Nothing to commit");
        }

        String newCommit = UUID.randomUUID().toString();
        String curCommit = state.getCurrentCommit();

        File backupDir = new File(Utils.backupDirName);
        File newCommitFolder = new File(backupDir, newCommit);
        File curCommitFolder = new File(backupDir, curCommit);

        if (!newCommitFolder.mkdir()) {
            throw new MyGitCommandExecError("Cant create folder for commit");
        }

        try {
            FileUtils.copyDirectory(curCommitFolder, newCommitFolder);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyGitCommandExecError("Cant copy files to new commit folder");
        }

        File rootRepoDir = new File(".");

        state.getFiles().stream().forEach(filePath -> {
            File file = new File(filePath);
            try {
                if(rootRepoDir.equals(file.getParentFile()) || file.getParentFile() == null) {
                    FileUtils.copyFileToDirectory(file, newCommitFolder);
                    return;
                }
                File parent = file.getParentFile();
                List<String> folders = new ArrayList<>();
                while (parent != null) {
                    folders.add(parent.getName());
                    parent = parent.getParentFile();
                }
                File folder = newCommitFolder;
                for (int i = folders.size() - 1; i >= 0; i--) {
                    folder = new File(folder, folders.get(i));
                }
                folder.mkdirs();
                FileUtils.copyFileToDirectory(file, folder);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("cant commit file \"" + file.getName() + "\"");
            }
        });


        String commitMsg = String.join(" ", args);
        state.addCommit(newCommit, commitMsg);
        state.setCurrentCommit(newCommit);
        String activeBranch = state.getActiveBranch();
        if(activeBranch != null) {
            state.removeBranch(activeBranch);
            state.addBranch(activeBranch, newCommit);
            state.setActiveBranch(activeBranch);
        }
        state.addParent(newCommit, curCommit);
        state.resetAddedFile();

        State.writeState(state);
    }

    @Override
    public String getName() {
        return "commit";
    }


}
