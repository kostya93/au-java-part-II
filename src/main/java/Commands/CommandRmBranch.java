package Commands;

import Util.MyGitCommandExecError;
import Util.State;

import java.util.List;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandRmBranch implements Command{
    @Override
    public void run(List<String> args) throws MyGitCommandExecError {
        State state = State.readState();
        args.stream().forEach(branchName -> {
            if (state.isBranchExist(branchName)) {
                state.removeBranch(branchName);
                System.out.println("Branch " + branchName + " was deleted");
            } else {
                System.out.println("branch \"" + branchName + "\" not found");
            }
        });
        State.writeState(state);
    }

    @Override
    public String getName() {
        return "rmbranch";
    }
}
