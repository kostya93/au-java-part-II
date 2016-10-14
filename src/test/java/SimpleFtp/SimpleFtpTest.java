package SimpleFtp;

import Client.Client;
import Client.ClientImpl;
import Client.MyFile;
import Common.Status;
import Server.Server;
import Server.ServerImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by kostya on 14.10.2016.
 */
public class SimpleFtpTest {
    final int PORT = 55555;
    final String HOST = "localhost";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testGet() throws IOException {
        Client client = new ClientImpl();
        Server server = new ServerImpl();

        folder.newFile("file");
        folder.newFolder("folder");

        int statusServer = server.start(PORT, folder.getRoot());
        assertEquals(Status.OK, statusServer);

        int statusClient = client.connect(HOST, PORT);
        assertEquals(Status.OK, statusClient);

        List<MyFile> currentList = client.executeList(".");
        assertEquals(2, currentList.size());
        assertEquals("file", currentList.get(0).getName());
        assertEquals(false, currentList.get(0).isDir());
        assertEquals("folder", currentList.get(1).getName());
        assertEquals(true, currentList.get(1).isDir());

        statusClient = client.disconnect();
        assertEquals(Status.OK, statusClient);

        statusServer = server.stop();
        assertEquals(Status.OK, statusServer);
    }

    @Test
    public void testList() throws IOException {
        Client client = new ClientImpl();
        Server server = new ServerImpl();

        File file = folder.newFile("file");
        byte[] realDate = "some data".getBytes();
        Files.write(file.toPath(), realDate);

        int statusServer = server.start(PORT + 1, folder.getRoot());
        assertEquals(Status.OK, statusServer);

        int statusClient = client.connect(HOST, PORT + 1);
        assertEquals(Status.OK, statusClient);

        byte[] curDate = client.executeGet("file");

        assertArrayEquals(realDate, curDate);

        statusClient = client.disconnect();
        assertEquals(Status.OK, statusClient);

        statusServer = server.stop();
        assertEquals(Status.OK, statusServer);
    }
}
