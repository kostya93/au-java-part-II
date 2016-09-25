package Commands;

import Util.MyGitCommandExecError;
import Util.State;

import java.util.List;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandMkBranch implements Command {
    @Override
    public void run(List<String> args) throws MyGitCommandExecError {
        State state = State.readState();
        args.stream().forEach(branchName -> {
            if (state.isBranchExist(branchName)) {
                System.out.println("branch \"" + branchName + "\" already exist");
            } else {
                state.addBranch(branchName, state.getCurrentCommit());
                System.out.println("branch \"" + branchName + "\" created");
            }
        });
        State.writeState(state);
    }

    @Override
    public String getName() {
        return "mkbranch";
    }
}
