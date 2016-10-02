package Commands;

import Util.MyGitCommandExecException;
import Util.Repository;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kostya on 02.10.2016.
 */
public class CommandRm extends AbstractCommand {
    @Override
    public List<String> run(List<String> args) throws MyGitCommandExecException {
        if (args.isEmpty()) {
            throw new MyGitCommandExecException("Command '" + getName() + "' needs a file name");
        }

        File rootDir = new File(".");
        File backupDir = new File(Repository.backupDirName);
        File stageDir = new File(backupDir, Repository.stageDirName);

        List<String> results = new ArrayList<>();

        args.forEach(fileName -> {
            File fileInStage = new File(stageDir, fileName);
            File fileInRoot = new File(rootDir, fileName);

            if (fileInRoot.exists()) {
                FileUtils.deleteQuietly(fileInRoot);
                results.add("File \"" + fileName + "\" was deleted");
            }

            if (fileInStage.exists()) {
                FileUtils.deleteQuietly(fileInStage);
                results.add("File \"" + fileName + "\" was deleted from stage");
            }
        });
        return results;
    }

    @Override
    public String getName() {
        return "rm";
    }
}
