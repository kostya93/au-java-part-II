package Commands;

import Util.MyGitCommandExecException;
import Util.Repository;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by kostya on 02.10.2016.
 */
public class CommandAddTest {
    File backupDir;
    File testFile;
    File stageDir;

    @Before
    public void init() throws IOException {
        backupDir = new File(Repository.backupDirName);
        stageDir = new File(backupDir, Repository.stageDirName);
        stageDir.mkdirs();

        String testFileName = UUID.randomUUID().toString();
        testFile = new File(testFileName);
        testFile.createNewFile();
    }
    @After
    public void clean() {
        FileUtils.deleteQuietly(backupDir);
        FileUtils.deleteQuietly(testFile);
    }

    @Test
    public void testRun() throws IOException, MyGitCommandExecException {
        Command commandAdd = new CommandAdd();
        List<String> results = commandAdd.run(Collections.singletonList(testFile.getName()));
        File fileInStage = new File(stageDir, testFile.getName());

        assertEquals(Collections.singletonList("File \"" + testFile.getName() + "\" added"), results);
        assertTrue(fileInStage.exists());
        assertTrue(FileUtils.contentEquals(testFile, fileInStage));
    }

    @Test
    public void testGetName() {
        Command commandAdd = new CommandAdd();
        assertEquals("add", commandAdd.getName());
    }
}