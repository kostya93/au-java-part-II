package Commands;

import Util.MyGitCommandExecError;
import Util.State;

import java.util.List;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandLog implements Command {
    @Override
    public void run(List<String> args) throws MyGitCommandExecError {
        State state = State.readState();
        String curCommit = state.getCurrentCommit();
        while (curCommit != null) {
            System.out.println(state.getCommitMessageByCommit(curCommit));
            curCommit = state.getParent(curCommit);
        }
    }

    @Override
    public String getName() {
        return "log";
    }
}
