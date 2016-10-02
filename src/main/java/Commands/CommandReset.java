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
public class CommandReset extends AbstractCommand {
    @Override
    public List<String> run(List<String> args) throws MyGitCommandExecException {
        if (args.isEmpty()) {
            throw new MyGitCommandExecException("Command '" + getName() + "' needs a file name");
        }

        File backupDir = new File(Repository.backupDirName);
        File stageDir = new File(backupDir, Repository.stageDirName);

        List<String> results = new ArrayList<>();
        args.forEach(fileName -> {
            File fileInStage = new File(stageDir, fileName);
            if (fileInStage.exists()) {
                FileUtils.deleteQuietly(fileInStage);
                results.add("File \"" + fileName + "\" was deleted from stage");
            } else {
                results.add("File \"" + fileName + "\" was not added");
            }
        });
        return results;
    }

    @Override
    public String getName() {
        return "reset";
    }
}
