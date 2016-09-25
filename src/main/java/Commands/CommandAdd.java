package Commands;

import Util.MyGitCommandExecError;
import Util.State;

import java.io.File;
import java.util.List;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandAdd implements Command {
    @Override
    public void run(List<String> args) throws MyGitCommandExecError {
        State state = State.readState();
        args.stream().forEach(fileName -> {
            File file = new File(fileName);
            if (file.exists()) {
                state.addFile(fileName);
                System.out.println("File \"" + fileName + "\" added");
            } else {
                System.out.println("File \"" + fileName + "\" not found");
            }
        });
        State.writeState(state);
    }

    @Override
    public String getName() {
        return "add";
    }
}
