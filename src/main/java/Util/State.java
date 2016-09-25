package Util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kostya on 25.09.2016.
 */
public class State implements Serializable {
    private HashMap<String, String> commitMessages;
    private HashMap<String, String> branchesToCommit;
    private HashMap<String, String> parents;
    private ArrayList<String> addedFiles;
    private String currentCommit;
    private String activeBranch;

    public String getCommitMessageByCommit(String commit) {
        return commitMessages.getOrDefault(commit, null);
    }

    public void addCommit(String commit, String commitMsg) {
        commitMessages.put(commit, commitMsg);
    }

    public boolean isBranchExist(String branch) {
        return branchesToCommit.containsKey(branch);
    }

    public boolean isCommitExist(String commit) {
        return commitMessages.containsKey(commit);
    }

    public void addBranch(String branch, String commit) {
        branchesToCommit.put(branch, commit);
    }

    public String getCommitByBranch(String branch) {
        return branchesToCommit.getOrDefault(branch, null);
    }

    public void removeBranch(String branch) {
        branchesToCommit.remove(branch);
        if (branch.equals(activeBranch)) {
            activeBranch = null;
        }
    }

    public void addFile(String file) {
        addedFiles.add(file);
    }

    public List<String> getFiles() {
        return addedFiles;
    }

    public void resetAddedFile() {
        addedFiles = new ArrayList<>();
    }

    public void addParent(String commit, String parent) {
        parents.put(commit, parent);
    }

    public String getParent(String commit) {
        return parents.get(commit);
    }

    public void setCurrentCommit(String commit){
        currentCommit = commit;
    }

    public String getCurrentCommit() {
        return currentCommit;
    }

    public String getActiveBranch() {
        return activeBranch;
    }

    public void setActiveBranch(String branch) {
        activeBranch = branch;
    }

    public State() {
        commitMessages = new HashMap<>();
        branchesToCommit = new HashMap<>();
        parents = new HashMap<>();
        addedFiles = new ArrayList<>();
    }

    public static State readState() throws MyGitCommandExecError {
        if (!Utils.isRepositoryExist()) {
            System.out.println("repository does not exist");
            System.exit(0);
        }

        File backupDir = new File(Utils.backupDirName);
        File stateFile = new File(backupDir, Utils.stateFileName);
        State state;
        try {
            FileInputStream fileInputStream = new FileInputStream(stateFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            state = (State) objectInputStream.readObject();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new MyGitCommandExecError("Error with read state from disk");
        }

        return state;
    }

    public static void writeState(State state) throws MyGitCommandExecError {
        File backupDir = new File(Utils.backupDirName);
        File stateFile = new File(backupDir, Utils.stateFileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(stateFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(state);
            objectOutputStream.close();
        } catch (IOException e) {
            throw new MyGitCommandExecError("Error with write state to disk");
        }
    }
}
