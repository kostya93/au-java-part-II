import Client.Client;
import Client.ClientImpl;
import Common.SharedFile;
import Tracker.SerializationException;
import Tracker.Tracker;
import Tracker.TrackerImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by kostya on 08.11.2016.
 */
public class Torrent {
    public static void main(String[] args) throws IOException, SerializationException {
        File root = new File(".");
        File serverRoot = new File(root, "serverRoot");
        serverRoot.mkdirs();
        Tracker tracker = new TrackerImpl(serverRoot);
        System.out.println("starting server...");
        tracker.start();
        System.out.println("server started");

        File clientRoot = new File(root, "clientRoot");
        clientRoot.mkdirs();
        Client client = new ClientImpl(12345, clientRoot);

        System.out.println("executing List...");
        List<SharedFile> res = client.executeList("localhost", Tracker.PORT);
        System.out.println("List executed");

        System.out.println("printing result...");
        res.forEach(System.out::println);
        System.out.println("result printed");

        System.out.println("stoping tracker...");
        tracker.stop();
        System.out.println("tracker stoped");
    }

}
