package Util;

import java.io.*;
import java.util.HashMap;

/**
 * Created by kostya on 25.09.2016.
 */
public class State implements Serializable {
    private HashMap<String, Commit> commits;
    private HashMap<String, Commit> branchesToCommit;
    private Commit currentCommit;
    private String activeBranch;

    public Commit getCommitByUUID(String uuid) {
        return commits.getOrDefault(uuid, null);
    }

    public void addCommit(Commit commit) {
        commits.put(commit.getUuid(), commit);
    }

    public boolean isBranchExist(String branch) {
        return branchesToCommit.containsKey(branch);
    }

    public boolean isCommitExistByUuid(String uuid) {
        return commits.containsKey(uuid);
    }

    public void addBranch(String branch, Commit commit) {
        branchesToCommit.put(branch, commit);
    }

    public Commit getCommitByBranch(String branch) {
        return branchesToCommit.getOrDefault(branch, null);
    }

    public void removeBranch(String branch) {
        branchesToCommit.remove(branch);
        if (branch.equals(activeBranch)) {
            activeBranch = null;
        }
    }

    public void setCurrentCommit(Commit commit){
        currentCommit = commit;
    }

    public Commit getCurrentCommit() {
        return currentCommit;
    }

    public String getActiveBranch() {
        return activeBranch;
    }

    public void setActiveBranch(String branch) {
        activeBranch = branch;
    }

    public State() {
        commits = new HashMap<>();
        branchesToCommit = new HashMap<>();
    }

    public static State readState() throws MyGitCommandExecException {
        File backupDir = new File(Repository.backupDirName);

        if (!(backupDir.exists() && backupDir.isDirectory())) {
            throw new MyGitCommandExecException("repository does not exist");
        }

        File stateFile = new File(backupDir, Repository.stateFileName);
        State state;
        try {
            FileInputStream fileInputStream = new FileInputStream(stateFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            state = (State) objectInputStream.readObject();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new MyGitCommandExecException("Error with read state from disk");
        }

        return state;
    }

    public static void writeState(State state) throws MyGitCommandExecException {
        File backupDir = new File(Repository.backupDirName);
        File stateFile = new File(backupDir, Repository.stateFileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(stateFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(state);
            objectOutputStream.close();
        } catch (IOException e) {
            throw new MyGitCommandExecException("Error with write state to disk");
        }
    }
}
