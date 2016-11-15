import Client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by kostya on 15.11.2016.
 */
public class ClientRepl {
    public static void main(String[] args) throws IOException {
        clientRepl();
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
        System.out.println("> enter \"stop\" to exit");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        ClientState state = ClientState.WAIT_PORT;
        Client client = new ClientImpl();
        int port = 0;
        String host;
        try {
            while (true) {
                switch (state) {
                    case WAIT_PORT:
                        System.out.println("> Enter SERVER_PORT");
                        System.out.print("> ");
                        port = Integer.parseInt(readLine(bufferedReader));
                        state = ClientState.WAIT_HOST;
                        break;
                    case WAIT_HOST:
                        System.out.println("> Enter SERVER_HOST");
                        System.out.print("> ");
                        host = readLine(bufferedReader);
                        client.connect(host, port);
                        System.out.println("> client connect to server: PORT = " + port + " HOST = " + host);
                        state = ClientState.WAIT_COMMAND;
                        break;
                    case WAIT_COMMAND:
                        System.out.println("> Enter COMMAND");
                        System.out.println("> 1 - get");
                        System.out.println("> 2 - list");
                        System.out.print("> ");
                        String command = readLine(bufferedReader);
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
                        String filePath = readLine(bufferedReader);
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
                        String folderPath = readLine(bufferedReader);
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
        } catch (StopProgramException e) {
            client.disconnect();
            System.out.println("Bye");
        }
    }

    private static String readLine(BufferedReader bufferedReader) throws IOException, StopProgramException {
        String line = bufferedReader.readLine();
        if (line.equals("stop")) {
            throw new StopProgramException();
        }
        return line;
    }
    private static class StopProgramException extends Exception {}
}
