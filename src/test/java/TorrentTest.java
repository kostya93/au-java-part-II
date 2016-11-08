import Client.Client;
import Client.ClientImpl;
import Common.SharedFile;
import Common.Source;
import Tracker.SerializationException;
import Tracker.Tracker;
import Tracker.TrackerImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by kostya on 08.11.2016.
 */
public class TorrentTest {
    private final String SERVER_HOST = "localhost";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testServerSimple() throws IOException, SerializationException {
        File rootServer = folder.newFolder("rootServer");
        Tracker tracker = new TrackerImpl(rootServer);
        tracker.start();
        tracker.stop();
    }

    @Test
    public void testClientSimple() throws IOException, SerializationException {
        final int CLIENT_PORT = 12345;
        File rootClient = folder.newFolder("rootClient");
        Client client = new ClientImpl(CLIENT_PORT, rootClient);
        client.start();
        client.stop();
    }

    @Test
    public void testUpload() throws IOException, SerializationException {
        File rootServer = folder.newFolder("rootServer");
        Tracker tracker = new TrackerImpl(rootServer);
        tracker.start();

        final int CLIENT_PORT = 12345;
        File rootClient = folder.newFolder("rootClient");
        Client client = new ClientImpl(CLIENT_PORT, rootClient);

        File someFile = new File(rootClient, "some_file.txt");
        if (!someFile.createNewFile()) {
            throw new IOException("cant create file");
        }

        client.executeUpload(SERVER_HOST, Tracker.PORT, someFile);

        tracker.stop();
    }

    @Test
    public void testList() throws IOException, SerializationException {
        File rootServer = folder.newFolder("rootServer");
        Tracker tracker = new TrackerImpl(rootServer);
        tracker.start();

        final int CLIENT_PORT = 12345;
        File rootClient = folder.newFolder("rootClient");
        Client client = new ClientImpl(CLIENT_PORT, rootClient);

        List<File> files = new ArrayList<>();
        files.add(new File(rootClient, "some_file_one.txt"));
        files.add(new File(rootClient, "some_file_two.txt"));
        files.add(new File(rootClient, "some_file_three.txt"));

        for (File file: files) {
            if (!file.createNewFile()) {
                throw new IOException("cant create file");
            }
        }

        List<Integer> ids = new ArrayList<>();

        for (File file: files) {
            ids.add(client.executeUpload(SERVER_HOST, Tracker.PORT, file));
        }

        List<SharedFile> sharedFiles = client.executeList(SERVER_HOST, Tracker.PORT);

        assertEquals(files.size(), ids.size());
        assertEquals(files.size(), sharedFiles.size());

        for (int i = 0; i < sharedFiles.size(); i++) {
            assertEquals((int)ids.get(i), sharedFiles.get(i).getId());
            assertEquals(files.get(i).getName(), sharedFiles.get(i).getName());
        }

        tracker.stop();
    }

    @Test
    public void testDownloadFile() throws IOException, SerializationException {
        File rootServer = folder.newFolder("rootServer");
        Tracker tracker = new TrackerImpl(rootServer);
        tracker.start();

        final int CLIENT_ONE_PORT = 12345;
        final String rootClientOneName = "rootClientOne";
        File rootClientOne = folder.newFolder(rootClientOneName);
        Client clientOne = new ClientImpl(CLIENT_ONE_PORT, rootClientOne);

        File someFile = new File(rootClientOne, "some_file.txt");
        if (!someFile.createNewFile()) {
            throw new IOException("cant create file");
        }

        byte[] content = "some content".getBytes();
        FileOutputStream fos = new FileOutputStream(someFile);
        fos.write(content);
        fos.close();

        clientOne.executeUpload(SERVER_HOST, Tracker.PORT, someFile);
        clientOne.executeUpdate(SERVER_HOST, Tracker.PORT);

        clientOne.start();

        final int CLIENT_TWO_PORT = 12346;
        final String rootClientTwoName = "rootClientTwo";
        File rootClientTwo = folder.newFolder(rootClientTwoName);
        Client clientTwo = new ClientImpl(CLIENT_TWO_PORT, rootClientTwo);

        SharedFile sharedFile = clientTwo.executeList(SERVER_HOST, Tracker.PORT).get(0);
        Source source = clientTwo.executeSources(SERVER_HOST, Tracker.PORT, sharedFile.getId()).get(0);
        clientTwo.executeGet(source, sharedFile, 0);

        File dirForFiles = new File(rootClientTwo, "files");
        File downloadedFile = new File(dirForFiles, Integer.toString(sharedFile.getId()));
        byte[] downloadedContent = Files.readAllBytes(downloadedFile.toPath());

        assertArrayEquals(content, downloadedContent);

        tracker.stop();
    }

}