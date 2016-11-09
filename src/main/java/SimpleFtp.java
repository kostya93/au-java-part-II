import Client.*;
import Common.SocketIOException;
import Server.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kostya on 14.10.2016.
 */
public class SimpleFtp {
    private enum ProgType {
        CLIENT,
        SERVER
    }
    public static void main(String[] args) throws RootDirectoryNotFound, IOException {
        switch (getProgTypeFromUser()) {
            case CLIENT:
                clientRepl();
                break;
            case SERVER:
                serverRepl();
                break;
        }
    }

    private enum ClientState {
        WAIT_PORT,
        WAIT_HOST,
        WAIT_COMMAND,
        WAIT_PATH_FOR_GET,
        WAIT_PATH_FOR_LIST
    }

    private static void clientRepl() throws IOException {
        System.out.println("> CLIENT");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        ClientState state = ClientState.WAIT_PORT;
        Client client = new ClientImpl();
        int port = 0;
        String host;
        while (true) {
            switch (state) {
                case WAIT_PORT:
                    System.out.println("> Enter SERVER_PORT");
                    System.out.print("> ");
                    port = Integer.parseInt(bufferedReader.readLine());
                    state = ClientState.WAIT_HOST;
                    break;
                case WAIT_HOST:
                    System.out.println("> Enter SERVER_HOST");
                    System.out.print("> ");
                    host = bufferedReader.readLine();
                    client.connect(host, port);
                    System.out.println("> client connect to server: PORT = " + port + " HOST = " + host);
                    state = ClientState.WAIT_COMMAND;
                    break;
                case WAIT_COMMAND:
                    System.out.println("> Enter COMMAND");
                    System.out.println("> 1 - get");
                    System.out.println("> 2 - list");
                    System.out.print("> ");
                    String command = bufferedReader.readLine();
                    switch (command) {
                        case "1":
                            state = ClientState.WAIT_PATH_FOR_GET;
                            break;
                        case "2":
                            state = ClientState.WAIT_PATH_FOR_LIST;
                            break;
                        default:
                            System.out.println("wrong COMMAND");
                            break;
                    }
                    break;
                case WAIT_PATH_FOR_GET:
                    System.out.println("> Enter PATH to file");
                    System.out.print("> ");
                    String filePath = bufferedReader.readLine();
                    byte[] content = client.executeGet(filePath);
                    if (content == null) {
                        System.out.println("> file not found");
                    } else {
                        System.out.println("> File content:");
                        System.out.println(new String(content, StandardCharsets.UTF_8));
                    }
                    state = ClientState.WAIT_COMMAND;
                    break;
                case WAIT_PATH_FOR_LIST:
                    System.out.println("> Enter PATH to folder");
                    System.out.print("> ");
                    String folderPath = bufferedReader.readLine();
                    List<MyFile> files = client.executeList(folderPath);
                    if (files == null) {
                        System.out.println("> folder not found");
                    } else {
                        System.out.println("> Folder content:");
                        files.forEach(System.out::println);
                    }
                    state = ClientState.WAIT_COMMAND;
                    break;
            }
        }
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
                            return;
                        } else {
                            System.out.println("> Wrong command");
                        }
                    }
            }
        }
    }

    private static ProgType getProgTypeFromUser() throws IOException {
        System.out.println("> select the type of program");
        System.out.println("> 1 - client");
        System.out.println("> 2 - server");
        System.out.print("> ");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = bufferedReader.readLine();
            switch (line) {
                case "1":
                    return ProgType.CLIENT;
                case "2":
                    return ProgType.SERVER;
                default:
                    System.out.println("> wrong input, try again");
                    System.out.println("> 1 - client");
                    System.out.println("> 2 - server");
                    System.out.print("> ");
                    break;
            }
        }
    }
}
