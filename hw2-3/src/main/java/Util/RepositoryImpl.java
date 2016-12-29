package Util;

import Commands.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by kostya on 02.10.2016.
 */
public class RepositoryImpl implements Repository {
    private Map<String, Command> commands;

    @Override
    public List<String> execCommand(String[] args) {
        if (args.length == 0) {
            return getHelp();
        }
        if (commands.containsKey(args[0])) {
            try {
                return commands.get(args[0]).run(Arrays.asList(args).subList(1, args.length));
            } catch (MyGitCommandExecException e) {
                return Collections.singletonList(e.getMessage());
            }
        } else {
            return Collections.singletonList("Command " + args[0] + " not found");
        }
    }

    public RepositoryImpl() {
        List<Command> commandList = Arrays.asList(
                new CommandInit(),
                new CommandAdd(),
                new CommandCommit(),
                new CommandMkBranch(),
                new CommandRmBranch(),
                new CommandCheckout(),
                new CommandLog(),
                new CommandMerge(),
                new CommandStatus(),
                new CommandReset(),
                new CommandRm(),
                new CommandClean()
        );
        commands = commandList.stream().collect(
                Collectors.toMap(Command::getName, command -> command)
        );
    }

    private List<String> getHelp() {
        return Collections.singletonList("help");
    }
}
