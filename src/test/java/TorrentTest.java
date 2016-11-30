import Client.Client;
import Client.ClientImpl;
import Common.SharedFile;
import Common.SocketIOException;
import Common.Source;
import Common.SerializationException;
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
import static org.junit.Assert.assertTrue;

/**
 * Created by kostya on 08.11.2016.
 */
public class TorrentTest {
    private final String SERVER_HOST = "localhost";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testServerSimple() throws IOException, SerializationException {
        final int SERVER_PORT = 55555;
        File rootServer = folder.newFolder("rootServer");
        Tracker tracker = new TrackerImpl();
        tracker.start(SERVER_PORT, rootServer);
        tracker.stop();
    }

    @Test
    public void testClientSimple() throws IOException, SerializationException {
        final int CLIENT_PORT = 44444;
        File rootClient = folder.newFolder("rootClient");
        Client client = new ClientImpl();
        client.start(CLIENT_PORT, rootClient);
        client.stop();
    }

    @Test
    public void testUpload() throws IOException, SerializationException {
        final int SERVER_PORT = 55556;
        File rootServer = folder.newFolder("rootServer");
        Tracker tracker = new TrackerImpl();
        tracker.start(SERVER_PORT, rootServer);

        final int CLIENT_PORT = 44445;
        File rootClient = folder.newFolder("rootClient");
        Client client = new ClientImpl();
        client.start(CLIENT_PORT, rootClient);

        File someFile = new File(rootClient, "some_file.txt");
        if (!someFile.createNewFile()) {
            throw new IOException("cant create file");
        }

        int fileId = client.executeUpload(SERVER_HOST, SERVER_PORT, someFile);
        assertEquals(0, fileId);
        client.stop();
        tracker.stop();
    }

    @Test
    public void testList() throws IOException, SerializationException {
        final int SERVER_PORT = 55557;
        File rootServer = folder.newFolder("rootServer");
        Tracker tracker = new TrackerImpl();
        tracker.start(SERVER_PORT, rootServer);

        final int CLIENT_PORT = 44446;
        File rootClient = folder.newFolder("rootClient");
        Client client = new ClientImpl();

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

        client.start(CLIENT_PORT, rootClient);

        for (File file: files) {
            ids.add(client.executeUpload(SERVER_HOST, SERVER_PORT, file));
        }

        List<SharedFile> sharedFiles = client.executeList(SERVER_HOST, SERVER_PORT);

        assertEquals(files.size(), ids.size());
        assertEquals(files.size(), sharedFiles.size());

        for (int i = 0; i < sharedFiles.size(); i++) {
            assertEquals((int)ids.get(i), sharedFiles.get(i).getId());
            assertEquals(files.get(i).getName(), sharedFiles.get(i).getName());
        }
        client.stop();
        tracker.stop();
    }

    @Test
    public void testGet() throws IOException, SerializationException {
        final int SERVER_PORT = 55558;
        File rootServer = folder.newFolder("rootServer");
        Tracker tracker = new TrackerImpl();
        tracker.start(SERVER_PORT, rootServer);

        final int CLIENT_ONE_PORT = 44447;
        final String rootClientOneName = "rootClientOne";
        File rootClientOne = folder.newFolder(rootClientOneName);
        Client clientOne = new ClientImpl();

        File someFile = new File(rootClientOne, "some_file.txt");
        if (!someFile.createNewFile()) {
            throw new IOException("cant create file");
        }

        byte[] content = "some content".getBytes();
        FileOutputStream fos = new FileOutputStream(someFile);
        fos.write(content);
        fos.close();

        clientOne.start(CLIENT_ONE_PORT, rootClientOne);

        clientOne.executeUpload(SERVER_HOST, SERVER_PORT, someFile);
        clientOne.executeUpdate(SERVER_HOST, SERVER_PORT);


        final int CLIENT_TWO_PORT = 44448;
        final String rootClientTwoName = "rootClientTwo";
        File rootClientTwo = folder.newFolder(rootClientTwoName);
        Client clientTwo = new ClientImpl();
        clientTwo.start(CLIENT_TWO_PORT, rootClientTwo);

        SharedFile sharedFile = clientTwo.executeList(SERVER_HOST, SERVER_PORT).get(0);
        Source source = clientTwo.executeSources(SERVER_HOST, SERVER_PORT, sharedFile.getId()).get(0);
        clientTwo.executeGet(source, sharedFile, 0);

        File dirForFiles = new File(rootClientTwo, "files");
        File downloadedFile = new File(dirForFiles, Integer.toString(sharedFile.getId()));
        byte[] downloadedContent = Files.readAllBytes(downloadedFile.toPath());

        assertArrayEquals(content, downloadedContent);

        clientOne.stop();
        clientTwo.stop();
        tracker.stop();
    }

    @Test
    public void testUpdate() throws IOException, SerializationException {
        final int SERVER_PORT = 55559;
        File rootServer = folder.newFolder("rootServer");
        Tracker tracker = new TrackerImpl();
        tracker.start(SERVER_PORT, rootServer);

        final int CLIENT_PORT = 44449;
        File rootClient = folder.newFolder("rootClient");
        Client client = new ClientImpl();
        client.start(CLIENT_PORT, rootClient);

        List<File> files = new ArrayList<>();
        files.add(new File(rootClient, "some_file_one.txt"));
        files.add(new File(rootClient, "some_file_two.txt"));

        for (File file: files) {
            if (!file.createNewFile()) {
                throw new IOException("cant create file");
            }
        }

        int id0 = client.executeUpload(SERVER_HOST, SERVER_PORT, files.get(0));
        assertEquals(0, id0);

        boolean update0 = client.executeUpdate(SERVER_HOST, SERVER_PORT);
        assertTrue(update0);

        List<Source> sources0 = client.executeSources(SERVER_HOST, SERVER_PORT, id0);
        assertEquals(1, sources0.size());
        assertEquals(CLIENT_PORT, sources0.get(0).getPort());

        List<SharedFile> sharedFiles0 = client.executeList(SERVER_HOST, SERVER_PORT);
        assertEquals(1, sharedFiles0.size());
        assertEquals(id0, sharedFiles0.get(0).getId());

        int id1 = client.executeUpload(SERVER_HOST, SERVER_PORT, files.get(1));
        assertEquals(1, id1);

        boolean update1 = client.executeUpdate(SERVER_HOST, SERVER_PORT);
        assertTrue(update1);

        List<Source> sources1 = client.executeSources(SERVER_HOST, SERVER_PORT, id1);
        assertEquals(1, sources1.size());
        assertEquals(CLIENT_PORT, sources1.get(0).getPort());

        List<SharedFile> sharedFiles1 = client.executeList(SERVER_HOST, SERVER_PORT);
        assertEquals(2, sharedFiles1.size());
        assertTrue(sharedFiles1.get(0).getId() == id1 || sharedFiles1.get(1).getId() == id1);
    }

    @Test
    public void testSources() throws IOException, SerializationException {
        final int SERVER_PORT = 55560;
        File rootServer = folder.newFolder("rootServer");
        Tracker tracker = new TrackerImpl();
        tracker.start(SERVER_PORT, rootServer);

        final int CLIENT_PORT = 44450;
        File rootClient = folder.newFolder("rootClient");
        Client client = new ClientImpl();
        client.start(CLIENT_PORT, rootClient);

        List<Source> sources = client.executeSources(SERVER_HOST, SERVER_PORT, 0);
        assertTrue(sources.isEmpty());

        File someFile = new File(rootClient, "some_file.txt");
        if (!someFile.createNewFile()) {
            throw new IOException("cant create file");
        }

        int id = client.executeUpload(SERVER_HOST, SERVER_PORT, someFile);
        client.executeUpdate(SERVER_HOST, SERVER_PORT);

        sources = client.executeSources(SERVER_HOST, SERVER_PORT, id);
        assertEquals(1, sources.size());
        assertEquals(CLIENT_PORT, sources.get(0).getPort());
    }

    @Test
    public void testStat() throws IOException, SerializationException {
        final int SERVER_PORT = 55561;
        File rootServer = folder.newFolder("rootServer");
        Tracker tracker = new TrackerImpl();
        tracker.start(SERVER_PORT, rootServer);

        final int CLIENT_ONE_PORT = 44452;
        final String rootClientOneName = "rootClientOne";
        File rootClientOne = folder.newFolder(rootClientOneName);
        Client clientOne = new ClientImpl();

        File someFile = new File(rootClientOne, "some_file.txt");
        if (!someFile.createNewFile()) {
            throw new IOException("cant create file");
        }
        try (FileOutputStream fos = new FileOutputStream(someFile)){
            fos.write("some content".getBytes());
        }

        clientOne.start(CLIENT_ONE_PORT, rootClientOne);
        int fileId = 42;

        final int CLIENT_TWO_PORT = 44453;
        final String rootClientTwoName = "rootClientTwo";
        File rootClientTwo = folder.newFolder(rootClientTwoName);
        Client clientTwo = new ClientImpl();
        clientTwo.start(CLIENT_TWO_PORT, rootClientTwo);

        List<Source> sources = clientTwo.executeSources(SERVER_HOST, SERVER_PORT, fileId);
        assertEquals(0, sources.size());

        fileId = clientOne.executeUpload(SERVER_HOST, SERVER_PORT, someFile);
        clientOne.executeUpdate(SERVER_HOST, SERVER_PORT);

        sources = clientTwo.executeSources(SERVER_HOST, SERVER_PORT, fileId);
        List<Integer> parts = clientTwo.executeStat(sources.get(0), fileId);

        assertEquals(1, parts.size());
        assertEquals(0, parts.get(0).intValue());

        clientOne.stop();
        clientTwo.stop();
        tracker.stop();
    }

    @Test
    public void testStoreRestoreStateClient() throws IOException, SerializationException {
        final int SERVER_PORT = 55562;
        File rootServer = folder.newFolder("rootServer");
        Tracker tracker = new TrackerImpl();
        tracker.start(SERVER_PORT, rootServer);

        final int CLIENT_PORT = 44454;
        final String rootClientName = "rootClient";
        File rootClient = folder.newFolder(rootClientName);
        Client client = new ClientImpl();
        client.start(CLIENT_PORT, rootClient);

        File someFile = new File(rootClient, "some_file.txt");
        if (!someFile.createNewFile()) {
            throw new IOException("cant create file");
        }

        int fileId = client.executeUpload(SERVER_HOST, SERVER_PORT, someFile);
        client.executeUpdate(SERVER_HOST, SERVER_PORT);

        tracker.stop();
        File anotherRootServer = folder.newFolder("anotherRootServer");
        tracker.start(SERVER_PORT, anotherRootServer);

        client.stop();
        client.start(CLIENT_PORT, rootClient);

        client.executeUpdate(SERVER_HOST, SERVER_PORT);

        List<Source> sources = client.executeSources(SERVER_HOST, SERVER_PORT, fileId);
        assertEquals(1, sources.size());
        assertEquals(CLIENT_PORT, sources.get(0).getPort());
    }

    @Test
    public void testStoreRestoreStateTracker() throws IOException, SerializationException {
        final int SERVER_PORT = 55563;
        File rootServer = folder.newFolder("rootServer");
        Tracker tracker = new TrackerImpl();
        tracker.start(SERVER_PORT, rootServer);

        final int CLIENT_PORT = 44455;
        final String rootClientName = "rootClient";
        File rootClient = folder.newFolder(rootClientName);
        Client client = new ClientImpl();
        client.start(CLIENT_PORT, rootClient);

        File someFile = new File(rootClient, "some_file.txt");
        if (!someFile.createNewFile()) {
            throw new IOException("cant create file");
        }

        int fileId = client.executeUpload(SERVER_HOST, SERVER_PORT, someFile);
        client.executeUpdate(SERVER_HOST, SERVER_PORT);

        List<Source> sources = client.executeSources(SERVER_HOST, SERVER_PORT, fileId);
        assertEquals(1, sources.size());
        assertEquals(CLIENT_PORT, sources.get(0).getPort());

        tracker.stop();
        tracker.start(SERVER_PORT, rootServer);

        sources = client.executeSources(SERVER_HOST, SERVER_PORT, fileId);
        assertEquals(1, sources.size());
        assertEquals(CLIENT_PORT, sources.get(0).getPort());
    }
}
