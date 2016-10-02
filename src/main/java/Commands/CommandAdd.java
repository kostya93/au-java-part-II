package Commands;

import Util.MyGitCommandExecException;
import Util.Repository;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kostya on 25.09.2016.
 */
public class CommandAdd extends AbstractCommand {
    @Override
    public List<String> run(List<String> args) throws MyGitCommandExecException {
        List<String> results = new ArrayList<>();
        File backupDir = new File(Repository.backupDirName);
        File stageDir = new File(backupDir, Repository.stageDirName);
        args.stream().forEach(fileName -> {
            File fileInCurDir = new File(fileName);
            File fileInStageDir = new File(stageDir, fileName);
            if (fileInCurDir.exists()) {
                try {
                    if (fileInStageDir.exists()) {
                        if (FileUtils.contentEquals(fileInCurDir, fileInStageDir)) {
                            results.add("File \"" + fileName + "\" already added");
                        } else {
                            FileUtils.deleteQuietly(fileInStageDir);
                        }
                    }
                    FileUtils.copyFile(fileInCurDir,fileInStageDir);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(0);
                }
                results.add("File \"" + fileName + "\" added");
            } else {
                results.add("File \"" + fileName + "\" not found");
            }
        });
        return results;
    }

    @Override
    public String getName() {
        return "add";
    }
}
