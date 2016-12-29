package Commands;

import Util.MyGitCommandExecException;

import java.util.List;

/**
 * Created by kostya on 25.09.2016.
 */
public interface Command {
    List<String> run(List<String> args) throws MyGitCommandExecException;
    String getName();
}
