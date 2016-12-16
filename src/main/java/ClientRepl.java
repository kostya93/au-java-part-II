import Client.*;
import Common.SerializationException;
import Common.SharedFile;
import Common.Source;

import java.io.*;
import java.util.List;

/**
 * Created by kostya on 30.11.2016.
 */
public class ClientRepl {
    public static void main(String[] args) throws IOException, SerializationException {
        clientRepl();
    }

    private enum ClientState {
        WAIT_SERVER_PORT,
        WAIT_SERVER_HOST,
        WAIT_CLIENT_PORT,
        WAIT_ROOT_DIR,
        WAIT_COMMAND,
    }

    private static class UserCommands {
        private final static String EXIT = "0";
        private final static String LIST = "1";
        private final static String UPLOAD = "2";
        private final static String SOURCES = "3";
        private final static String UPDATE = "4";
        private final static String STAT = "5";
        private final static String GET = "6";
        private final static String ADD_FILE_TO_DOWNLOADING = "7";
        private final static String CHECK_DOWNLOADING_FILES_STATE = "8";
    }

    private static void clientRepl() throws IOException, SerializationException {
        System.out.println("> CLIENT");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        ClientState state = ClientState.WAIT_SERVER_PORT;
        Client client = new ClientImpl();
        int serverPort = 55555;
        String serverHost = "localhost";
        int clientPort = 44444;
        File rootDir;
        while (true) {
            switch (state) {
                case WAIT_SERVER_PORT:
                    System.out.println("> Enter SERVER_PORT(or nothing for 55555)");
                    System.out.print("> ");
                    String serverPortStr = bufferedReader.readLine();
                    if (serverPortStr.isEmpty()) {
                        state = ClientState.WAIT_SERVER_HOST;
                        break;
                    }
                    try {
                        serverPort = Integer.parseInt(serverPortStr);
                    } catch (NumberFormatException e) {
                        System.out.print("> wrong port, try again");
                        break;
                    }
                    state = ClientState.WAIT_SERVER_HOST;
                    break;
                case WAIT_SERVER_HOST:
                    System.out.println("> Enter SERVER_HOST(or nothing for \"localhost\")");
                    System.out.print("> ");
                    String host = bufferedReader.readLine();
                    state = ClientState.WAIT_CLIENT_PORT;
                    if (host.isEmpty()) {
                        break;
                    }
                    serverHost = host;
                    break;
                case WAIT_CLIENT_PORT:
                    System.out.println("> Enter current CLIENT_PORT(or nothing for 44444)");
                    System.out.print("> ");
                    String clientPortStr = bufferedReader.readLine();
                    if (clientPortStr.isEmpty()) {
                        state = ClientState.WAIT_ROOT_DIR;
                        break;
                    }
                    try {
                        clientPort = Integer.parseInt(clientPortStr);
                    } catch (NumberFormatException e) {
                        System.out.print("> wrong port, try again");
                        break;
                    }
                    state = ClientState.WAIT_ROOT_DIR;
                    break;
                case WAIT_ROOT_DIR:
                    System.out.println("> Enter ROOT_DIR (or nothing for ./client_1)");
                    System.out.print("> ");
                    String rootDirName = bufferedReader.readLine();
                    if (rootDirName.isEmpty()) {
                        rootDir = new File(new File("."), "client_1");
                    } else {
                        rootDir = new File(rootDirName);
                    }
                    rootDir.mkdirs();
                    client.start(clientPort, rootDir);
                    System.out.println("> Client started on PORT = " + serverPort + "; ROOT_DIR = " + rootDir.getAbsolutePath());
                    state = ClientState.WAIT_COMMAND;
                    break;
                case WAIT_COMMAND:
                    printHelp();
                    String command = bufferedReader.readLine();
                    boolean continueProcessing = processInput(bufferedReader, client, serverPort, serverHost, command);
                    if (!continueProcessing) {
                        return;
                    }
            }
        }
    }

    private static boolean processInput(BufferedReader bufferedReader, Client client, int serverPort, String serverHost, String command) throws SerializationException, IOException {
        switch (command) {
            case UserCommands.EXIT:
                System.out.println("> command EXIT");
                client.stop();
                System.out.println("> Bye.");
                return false;
            case UserCommands.LIST:
                System.out.println("> command LIST");
                List<SharedFile> files = client.executeList(serverHost, serverPort);
                if (files.isEmpty()) {
                    System.out.println("No files");
                } else {
                    files.forEach(System.out::println);
                }
                break;
            case UserCommands.UPLOAD:
                System.out.println("> command UPLOAD");
                System.out.println("> Enter absolute path to file");
                String path = bufferedReader.readLine();
                File file = new File(path);
                int id;
                try {
                    id = client.executeUpload(serverHost, serverPort, file);
                } catch (FileNotFoundException e) {
                    System.out.println("> file \"" + path + "\" not found");
                    break;
                }
                System.out.println("> file uploaded; id = " + id);
                break;
            case UserCommands.SOURCES:
                System.out.println("> command SOURCES");
                Integer fileId = getIntegerFromUser(bufferedReader, "file id");
                if (fileId == null) {
                    break;
                }
                List<Source> sources = client.executeSources(serverHost, serverPort, fileId);
                sources.forEach(System.out::println);
                break;
            case UserCommands.UPDATE:
                System.out.println("> command UPDATE");
                System.out.println("update status: " + client.executeUpdate(serverHost, serverPort));
                break;
            case UserCommands.STAT:
                System.out.println("> command STAT");
                Source sourceToStat = getSourceFromUser(bufferedReader);
                if (sourceToStat == null) {
                    break;
                }
                Integer fileIdToStat = getIntegerFromUser(bufferedReader, "file id");
                if (fileIdToStat == null) {
                    break;
                }
                System.out.println("> file part");
                List<Integer> parts = client.executeStat(sourceToStat, fileIdToStat);
                parts.forEach(System.out::println);
                break;
            case UserCommands.GET:
                System.out.println("> command GET");
                Source sourceToGet = getSourceFromUser(bufferedReader);
                if (sourceToGet == null) {
                    break;
                }
                Integer fileIdToGet = getIntegerFromUser(bufferedReader, "file id");
                if (fileIdToGet == null) {
                    break;
                }
                System.out.println("> inter file size");
                long fileSize;
                try {
                    fileSize = Long.parseLong(bufferedReader.readLine());
                } catch (NumberFormatException e) {
                    System.out.println("wring size");
                    break;
                }

                Integer numberOfPart = getIntegerFromUser(bufferedReader, "number of part");
                if (numberOfPart == null) {
                    break;
                }

                System.out.println("> inter file name");

                client.executeGet(sourceToGet, new SharedFile(bufferedReader.readLine(), fileIdToGet, fileSize), numberOfPart);
                break;
            case UserCommands.ADD_FILE_TO_DOWNLOADING:
                System.out.println("> command ADD_FILE_TO_DOWNLOADING");
                Integer fileIdToDownloading = getIntegerFromUser(bufferedReader, "file id");
                if (fileIdToDownloading == null) {
                    break;
                }
                System.out.println("> inter file size");
                long fileSizeToDownloading;
                try {
                    fileSizeToDownloading = Long.parseLong(bufferedReader.readLine());
                } catch (NumberFormatException e) {
                    System.out.println("wring size");
                    break;
                }
                System.out.println("> inter file name");
                SharedFile sharedFile = new SharedFile(bufferedReader.readLine(), fileIdToDownloading, fileSizeToDownloading);
                client.addFileToDownloading(serverHost, serverPort, sharedFile);
                System.out.println("> file added to downloading");
                break;
            case UserCommands.CHECK_DOWNLOADING_FILES_STATE:
                System.out.println("> command CHECK DOWNLOADING FILES STATE");
                List<DownloadingFileState> list = client.downloadingState();
                if (list.isEmpty()) {
                    System.out.println("No files");
                } else {
                    list.forEach(System.out::println);
                }
                break;
            default:
                System.out.println("> wrong command");
                break;
        }
        return true;
    }

    private static void printHelp() {
        System.out.println("> Enter COMMAND");
        System.out.println("> ");
        System.out.println(String.format("> %s - EXIT", UserCommands.EXIT));
        System.out.println("> ");
        System.out.println("> Commands to server:");
        System.out.println(String.format("> %s - LIST", UserCommands.LIST));
        System.out.println(String.format("> %s - UPLOAD", UserCommands.UPLOAD));
        System.out.println(String.format("> %s - SOURCES", UserCommands.SOURCES));
        System.out.println(String.format("> %s - UPDATE", UserCommands.UPDATE));
        System.out.println("> ");
        System.out.println("> Commands to other client");
        System.out.println(String.format("> %s - STAT", UserCommands.STAT));
        System.out.println(String.format("> %s - GET", UserCommands.GET));
        System.out.println("> ");
        System.out.println("> Other commands");
        System.out.println(String.format("> %s - ADD FILE TO DOWNLOADING", UserCommands.ADD_FILE_TO_DOWNLOADING));
        System.out.println(String.format("> %s - CHECK DOWNLOADING FILES STATE", UserCommands.CHECK_DOWNLOADING_FILES_STATE));
        System.out.print("> ");
    }

    private static Source getSourceFromUser(BufferedReader bufferedReader) throws IOException {
        System.out.println("> inter source in format IP:PORT (example 127.0.0.1:1234)");
        String[] source = bufferedReader.readLine().split("\\.|:");
        byte[] sourceIp = new byte[4];
        int sourcePort;
        try {
            for (int i = 0; i < 4; i ++) {
                sourceIp[i] = Byte.parseByte(source[i]);
            }
            sourcePort = Integer.parseInt(source[4]);
        } catch (Exception e) {
            System.out.println("> wrong source format");
            return null;
        }
        return new Source(sourceIp, sourcePort);
    }

    private static Integer getIntegerFromUser(BufferedReader bufferedReader, String what) throws IOException {
        int fileId;
        System.out.println("> inter " + what);
        try {
            fileId = Integer.parseInt(bufferedReader.readLine());
        } catch (NumberFormatException e) {
            System.out.println("> wrong " + what);
            return null;
        }
        return fileId;
    }
}
