import Commands.*;
import Util.MyGitCommandExecError;
import Util.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by kostya on 25.09.2016.
 */
public class MyGit {
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.printHelp();
            return;
        }

        List<Command> commandList = Arrays.asList(
                new CommandInit(),
                new CommandAdd(),
                new CommandCommit(),
                new CommandMkBranch(),
                new CommandRmBranch(),
                new CommandCheckout(),
                new CommandLog(),
                new CommandMerge()
        );

        Map<String, Command> commands = commandList.stream().collect(
                Collectors.toMap(Command::getName, command -> command)
        );

        if (commands.containsKey(args[0])) {
            try {
                commands.get(args[0]).run(Arrays.asList(args).subList(1, args.length));
            } catch (MyGitCommandExecError error) {
                System.out.println(error.getMessage());
            }
        } else {
            System.out.println("Command " + args[0] + " not found");
        }
    }
}
