package Commands;

import Util.MyGitCommandExecException;
import Util.State;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandRmBranch extends AbstractCommand {
    @Override
    public List<String> run(List<String> args) throws MyGitCommandExecException {
        State state = State.readState();
        List<String> results = new ArrayList<>();
        args.forEach(branchName -> {
            if (state.isBranchExist(branchName)) {
                state.removeBranch(branchName);
                results.add("Branch " + branchName + " was deleted");
            } else {
                System.out.println("branch \"" + branchName + "\" not found");
            }
        });
        State.writeState(state);
        return results;
    }

    @Override
    public String getName() {
        return "rmbranch";
    }
}
