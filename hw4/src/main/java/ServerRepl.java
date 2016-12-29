import Server.RootDirectoryNotFound;
import Server.Server;
import Server.ServerImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by kostya on 14.10.2016.
 */
public class ServerRepl {
    public static void main(String[] args) throws RootDirectoryNotFound, IOException {
        serverRepl();
    }

    private enum ServerState {
        SELECT_PORT,
        SELECT_ROOT_DIR,
        WAIT_STOP
    }

    private static void serverRepl() throws IOException, RootDirectoryNotFound {
        System.out.println("> SERVER");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        ServerState state = ServerState.SELECT_PORT;
        int serverPort = 0;
        File rootDir;
        Server server = new ServerImpl();
        while (true) {
            switch (state) {
                case SELECT_PORT:
                    System.out.println("> Enter PORT");
                    System.out.print("> ");
                    String port = bufferedReader.readLine();
                    serverPort = Integer.parseInt(port);
                    state = ServerState.SELECT_ROOT_DIR;
                    break;
                case SELECT_ROOT_DIR:
                    System.out.println("> Enter ROOT_DIR (or nothing for current)");
                    System.out.print("> ");
                    String rootDirName = bufferedReader.readLine();
                    if (rootDirName.equals("")) {
                        rootDir = new File(".");
                    } else {
                        rootDir = new File(rootDirName);
                    }
                    server.start(serverPort, rootDir);
                    System.out.println("> Server started on PORT = " + serverPort + "; ROOT_DIR = " + rootDir.getAbsolutePath());
                    state = ServerState.WAIT_STOP;
                    break;
                case WAIT_STOP:
                    while (true) {
                        System.out.println("> Enter \"stop\" for stoping server");
                        System.out.print("> ");
                        String line = bufferedReader.readLine();
                        if (line.equals("stop")) {
                            server.stop();
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
