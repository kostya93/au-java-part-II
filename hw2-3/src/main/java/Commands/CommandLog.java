package Commands;

import Util.Commit;
import Util.MyGitCommandExecException;
import Util.State;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandLog extends AbstractCommand {
    @Override
    public List<String> run(List<String> args) throws MyGitCommandExecException {
        State state = State.readState();
        Commit curCommit = state.getCurrentCommit();
        List<String> results = new ArrayList<>();
        while (curCommit != null) {
            results.add(curCommit.toString());
            curCommit = curCommit.getParent();
        }
        return results;
    }

    @Override
    public String getName() {
        return "log";
    }
}
