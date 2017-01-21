package Commands;

import Util.MyGitCommandExecError;
import Util.State;

import java.util.List;

/**
 * Created by kostya on 25.09.2016.
 */
public interface Command {
    void run(List<String> args) throws MyGitCommandExecError;
    String getName();
}
