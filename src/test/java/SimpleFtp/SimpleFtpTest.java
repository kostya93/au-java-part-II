package SimpleFtp;

import Client.Client;
import Client.ClientImpl;
import Client.MyFile;
import Server.RootDirectoryNotFound;
import Server.Server;
import Server.ServerImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by kostya on 14.10.2016.
 */
public class SimpleFtpTest {
    private final int PORT = 55555;
    private final String HOST = "localhost";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testList() throws IOException, RootDirectoryNotFound {
        Client client = new ClientImpl();
        Server server = new ServerImpl();

        folder.newFile("file");
        folder.newFolder("folder");

        server.start(PORT, folder.getRoot());

        client.connect(HOST, PORT);

        List<MyFile> currentList = client.executeList(".");
        assertEquals(2, currentList.size());
        assertEquals("file", currentList.get(0).getName());
        assertEquals(false, currentList.get(0).isDir());
        assertEquals("folder", currentList.get(1).getName());
        assertEquals(true, currentList.get(1).isDir());
    }

    @Test
    public void testGet() throws IOException, RootDirectoryNotFound {
        Client client = new ClientImpl();
        Server server = new ServerImpl();

        File file = folder.newFile("file");
        byte[] realDate = "some data".getBytes();
        Files.write(file.toPath(), realDate);

        server.start(PORT + 1, folder.getRoot());
        client.connect(HOST, PORT + 1);

        byte[] curDate = client.executeGet("file");

        assertArrayEquals(realDate, curDate);

        client.disconnect();
        server.stop();
    }

    @Test
    public void testGetWithWrongFilename() throws RootDirectoryNotFound, IOException {
        Client client = new ClientImpl();
        Server server = new ServerImpl();

        server.start(PORT + 2, folder.getRoot());
        client.connect(HOST, PORT + 2);

        byte[] curDate = client.executeGet("wrong-filename");
        assertNull(curDate);

        client.disconnect();
        server.stop();
    }

    @Test
    public void testFewClients() throws IOException, RootDirectoryNotFound, InterruptedException {
        List<Client> clients = new ArrayList<Client>(Arrays.asList(new ClientImpl(), new ClientImpl(), new ClientImpl()));
        Server server = new ServerImpl();

        File file = folder.newFile("file");
        byte[] realDate = "some data".getBytes();
        Files.write(file.toPath(), realDate);

        server.start(PORT + 2, folder.getRoot());
        for (Client client : clients) {
            client.connect(HOST, PORT + 2);
        }

        List<Thread> threads = new ArrayList<>();
        List<byte[]> results = new ArrayList<>();

        for (int i = 0; i < clients.size(); i++) {
            final int n = i;
            threads.add(new Thread(() -> {
                byte[] result = clients.get(n).executeGet("file");
                synchronized (results) {
                    results.add(result);
                }
            }));
        }

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(clients.size(), results.size());
        for (byte[] res : results) {
            assertArrayEquals(realDate, res);
        }

        for (Client client : clients) {
            client.disconnect();
        }
        server.stop();
    }
}
