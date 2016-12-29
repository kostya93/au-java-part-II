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
public class CommandMerge extends AbstractCommand {
    @Override
    public List<String> run(List<String> args) throws MyGitCommandExecException {
        State state = State.readState();

        String branchToMerge = getBranchToMerge(state, args);

        Commit commitToMerge = state.getCommitByBranch(branchToMerge);
        Commit curCommit = state.getCurrentCommit();

        File backupDir = new File(Repository.backupDirName);
        File toMergeCommitDir = new File(backupDir, commitToMerge.getUuid());
        File curCommitDir = new File(backupDir, curCommit.getUuid());

        String conflictedFile = conflictedFile(curCommit, curCommitDir, commitToMerge, toMergeCommitDir);
        if (conflictedFile != null) {
            throw new MyGitCommandExecException("conflict if file \'" + conflictedFile + "\'");
        }

        Commit newCommit = new Commit();
        configNewCommit(newCommit, curCommit, commitToMerge, branchToMerge);

        File newCommitDir = new File(backupDir, newCommit.getUuid());
        newCommitDir.mkdir();

        copyFileBetweenCommit(curCommit, curCommitDir, newCommit, newCommitDir, curCommit.getFiles());

        Set<String> diff = new HashSet<>();
        diff.addAll(commitToMerge.getFiles());
        diff.removeAll(curCommit.getFiles());

        copyFileBetweenCommit(commitToMerge, toMergeCommitDir, newCommit, newCommitDir, diff);

        RemoveTrackedFilesFromRoot(state);
        copyFileToRootDir(newCommitDir);

        configState(state, newCommit);

        State.writeState(state);
        return Collections.singletonList("command \'" + getName() + "\' was finished");
    }

    private void copyFileToRootDir(File newCommitDir) {
        File rootDir = new File(".");
        try {
            FileUtils.copyDirectory(newCommitDir, rootDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configNewCommit(Commit newCommit, Commit curCommit, Commit commitToMerge, String branchToMerge) {
        newCommit.setUuid(UUID.randomUUID().toString());
        newCommit.setMessage("merge branch'" + branchToMerge + "\'");
        newCommit.setDate(new Date().toString());
        newCommit.setParent(curCommit);
        newCommit.addAllFiles(curCommit.getFiles());
        newCommit.addAllFiles(commitToMerge.getFiles());
    }

    private void configState(State state, Commit newCommit) {
        state.addCommit(newCommit);
        state.setCurrentCommit(newCommit);
        String activeBranch = state.getActiveBranch();
        state.removeBranch(activeBranch);
        state.addBranch(activeBranch, newCommit);
        state.setActiveBranch(activeBranch);
    }

    private String getBranchToMerge(State state, List<String> args) throws MyGitCommandExecException {
        if (args.isEmpty()) {
            throw new MyGitCommandExecException("Command merge needs a branch name");
        }

        String branchToMerge = args.get(0);

        if (branchToMerge.equals(state.getActiveBranch())) {
            throw new MyGitCommandExecException("We already here (" + branchToMerge + ")");
        }

        if(!state.isBranchExist(branchToMerge)) {
            throw new MyGitCommandExecException("Branch \"" + branchToMerge + "\" not found");
        }

        return branchToMerge;
    }

    private void copyFileBetweenCommit(Commit fromCommit, File fromCommitDir, Commit toCommit, File toCommitDir, Set<String> files) {
        for (String fileName: files) {
            File curFile = getFileByCommit(fromCommit, fileName);
            File dirForFile = getDirForFile(toCommitDir, fromCommit, curFile);
            try {
                FileUtils.copyFileToDirectory(curFile, dirForFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getDirForFile(File newDir, Commit curCommit, File curFile) {
        File backupDir = new File(Repository.backupDirName);
        File curDir = new File(backupDir, curCommit.getUuid());
        while (!FileUtils.listFiles(curDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE).contains(curFile)) {
            curCommit = curCommit.getParent();
            curDir = new File(backupDir, curCommit.getUuid());
        }
        File parentDirInNewDir = new File(newDir, curDir.toPath().relativize(curFile.toPath()).toString());
        parentDirInNewDir = parentDirInNewDir.getParentFile();
        if (!parentDirInNewDir.exists()) {
            parentDirInNewDir.mkdirs();
        }
        return parentDirInNewDir;
    }


    private File getFileByCommit(Commit curCommit, String fileName) {
        File backupDir = new File(Repository.backupDirName);
        while (curCommit != null) {
            File curCommitDir = new File(backupDir, curCommit.getUuid());
            for(File file: FileUtils.listFiles(curCommitDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE)) {
                if (curCommitDir.toPath().relativize(file.toPath()).toString().equals(fileName)) {
                    return file;
                }
            }
            curCommit = curCommit.getParent();
        }
        return null;
    }

    private String conflictedFile(Commit curCommit, File curCommitDir, Commit commitToMerge, File toMergeCommitDir) {
        Set<String> maybeConflict = new HashSet<>();
        maybeConflict.addAll(curCommit.getFiles());
        maybeConflict.retainAll(commitToMerge.getFiles());

        for(String fileName: maybeConflict) {
            File fileInCurCommitDir = new File(curCommitDir, fileName);
            File fileInCommitToMergeDir = new File(toMergeCommitDir, fileName);
            try {
                if (!FileUtils.contentEquals(fileInCommitToMergeDir, fileInCurCommitDir)) {
                    return fileName;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "merge";
    }
}