import Common.SerializationException;
import Tracker.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by kostya on 30.11.2016.
 */
public class TrackerRepl {
    public static void main(String[] args) throws IOException, SerializationException {
        trackerRepl();
    }

    private enum ServerState {
        SELECT_PORT,
        SELECT_ROOT_DIR,
        WAIT_STOP
    }

    private static void trackerRepl() throws IOException, SerializationException {
        System.out.println("> SERVER");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        ServerState state = ServerState.SELECT_PORT;
        int serverPort = 55555;
        File rootDir;
        Tracker tracker = new TrackerImpl();
        while (true) {
            switch (state) {
                case SELECT_PORT:
                    System.out.println("> Enter PORT(or nothing for 55555)");
                    System.out.print("> ");
                    String port = bufferedReader.readLine();
                    if (port.equals("")) {
                        state = ServerState.SELECT_ROOT_DIR;
                        break;
                    }
                    try {
                        serverPort = Integer.parseInt(port);
                    } catch (NumberFormatException e) {
                        System.out.print("> wrong port, try again");
                        break;
                    }
                    state = ServerState.SELECT_ROOT_DIR;
                    break;
                case SELECT_ROOT_DIR:
                    System.out.println("> Enter ROOT_DIR (absolute path, or nothing for ./server)");
                    System.out.print("> ");
                    String rootDirName = bufferedReader.readLine();
                    if (rootDirName.equals("")) {
                        rootDir = new File (new File("."), "server");
                    } else {
                        rootDir = new File(rootDirName);
                    }
                    rootDir.mkdirs();
                    tracker.start(serverPort, rootDir);
                    System.out.println("> Server started on PORT = " + serverPort + "; ROOT_DIR = " + rootDir.getAbsolutePath());
                    state = ServerState.WAIT_STOP;
                    break;
                case WAIT_STOP:
                    while (true) {
                        System.out.println("> Enter \"stop\" for stoping server");
                        System.out.print("> ");
                        String line = bufferedReader.readLine();
                        if (line.equals("stop")) {
                            tracker.stop();
                            System.out.println("Bye.");
                            return;
                        } else {
                            System.out.println("> Wrong command");
                        }
                    }
            }
        }
    }
}
