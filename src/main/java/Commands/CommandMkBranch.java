package Commands;

import Util.MyGitCommandExecException;
import Util.State;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandMkBranch extends AbstractCommand {
    @Override
    public List<String> run(List<String> args) throws MyGitCommandExecException {
        State state = State.readState();
        List<String> results = new ArrayList<>();
        args.forEach(branchName -> {
            if (state.isBranchExist(branchName)) {
                results.add("branch \"" + branchName + "\" already exist");
            } else {
                state.addBranch(branchName, state.getCurrentCommit());
                results.add("branch \"" + branchName + "\" created");
            }
        });
        State.writeState(state);
        return results;
    }

    @Override
    public String getName() {
        return "mkbranch";
    }
}
