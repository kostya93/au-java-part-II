package ClientGui;

import Client.Client;
import Client.ClientImpl;
import Client.DownloadingFileState;
import Common.SerializationException;
import Common.SharedFile;
import Common.SocketIOException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

/**
 * Created by kostya on 02.12.2016.
 */

public class ClientGuiController implements Initializable {
    private final Client client = new ClientImpl();
    private ClientGui clientGui;
    private Thread updater;

    private int serverPort;
    private String serverHost;

    private final ObservableList<AvailableFile> availableFilesData = FXCollections.observableArrayList();
    private final ObservableList<DownloadedFile> downloadedFilesData = FXCollections.observableArrayList();

    @FXML
    private Label statusLabel;

    @FXML
    private TextField serverPortTextField;

    @FXML
    private TextField serverHostTextField;

    @FXML
    private TextField clientPortTextField;

    @FXML
    private TextField clientRootDirTextField;

    @FXML
    private TableView<AvailableFile> availableFilesTableView;

    @FXML
    private TableColumn<AvailableFile, Integer> availableFilesIdColumn;

    @FXML
    private TableColumn<AvailableFile, Long> availableFilesSizeColumn;

    @FXML
    private TableColumn<AvailableFile, String> availableFilesNameColumn;

    @FXML
    private TableColumn<AvailableFile, Boolean> availableFilesActionColumn;

    @FXML
    private TableView<DownloadedFile> downloadedFilesTableView;

    @FXML
    private TableColumn<DownloadedFile, Integer> downloadedFilesIdColumn;

    @FXML
    private TableColumn<DownloadedFile, String> downloadedFilesNameColumn;

    @FXML
    private TableColumn<DownloadedFile, Double> downloadedFilesDoneColumn;

    @FXML
    private TableColumn<DownloadedFile, Long> downloadedFilesSizeColumn;

    @FXML
    private TableColumn<DownloadedFile, String> downloadedFilesPathColumn;

    void setMainApp(ClientGui mainApp) {
        this.clientGui = mainApp;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        availableFilesIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        availableFilesNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        availableFilesSizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        availableFilesActionColumn.setCellValueFactory(param -> new SimpleBooleanProperty(param.getValue() != null));
        availableFilesActionColumn.setCellFactory(param -> new ButtonCell());
        availableFilesTableView.setItems(availableFilesData);

        downloadedFilesIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        downloadedFilesNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        downloadedFilesDoneColumn.setCellValueFactory(new PropertyValueFactory<>("done"));
        downloadedFilesDoneColumn.setCellFactory(ProgressBarTableCell.forTableColumn());
        downloadedFilesSizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        downloadedFilesPathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        downloadedFilesTableView.setItems(downloadedFilesData);
    }

    private void updateData() {
        while (!Thread.currentThread().isInterrupted()) {
            if (client.isStarted()) {
                List<DownloadingFileState> downloadingFileStates = client.downloadingState();
                downloadedFilesData.clear();
                downloadedFilesData.addAll(
                        downloadingFileStates.stream()
                                .map(f -> new DownloadedFile(
                                        f.getSharedFile().getId(),
                                        f.getSharedFile().getName(),
                                        f.getProgress(),
                                        f.getSharedFile().getSize(),
                                        f.getPath()))
                                .collect(Collectors.toList())
                );
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    @FXML
    void handleStop() {
        if (!client.isStarted()) {
            showInformationDialog("client already stoped");
            return;
        }
        updater.interrupt();
        try {
            client.stop();
        } catch (SocketIOException | SerializationException e) {
            showInformationDialog(e.getMessage());
            return;
        }
        updater.interrupt();
        updater = null;
        statusLabel.setText("OFF");
        statusLabel.setTextFill(Paint.valueOf("RED"));
    }

    @FXML
    void handleStart() {
        if (client.isStarted()) {
            showInformationDialog("client already started");
            return;
        }

        if ("".equals(serverPortTextField.getText())) {
            showInformationDialog("inter server port number");
            return;
        }

        try {
            serverPort = Integer.parseInt(serverPortTextField.getText());
        } catch (NumberFormatException e) {
            showInformationDialog("server port number must be an integer");
            return;
        }

        if ("".equals(serverHostTextField.getText())) {
            showInformationDialog("inter server host");
            return;
        }

        serverHost = serverHostTextField.getText();

        int clientPort;

        if ("".equals(clientPortTextField.getText())) {
            showInformationDialog("inter client port number");
            return;
        }

        try {
            clientPort = Integer.parseInt(clientPortTextField.getText());
        } catch (NumberFormatException e) {
            showInformationDialog("client port number must be an integer");
            return;
        }

        if ("".equals(clientRootDirTextField.getText())) {
            showInformationDialog("inter client root dir");
            return;
        }

        File rootDir = new File(clientRootDirTextField.getText());
        rootDir.mkdirs();

        try {
            client.start(clientPort, rootDir);
        } catch (SocketIOException | SerializationException e) {
            showInformationDialog(e.getMessage());
        }
        updater = new Thread(this::updateData);
        updater.start();
        statusLabel.setText("ON");
        statusLabel.setTextFill(Paint.valueOf("GREEN"));
    }

    @FXML
    void handleUpload() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Upload File");
        File defaultDirectory = new File(".");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedFile = chooser.showOpenDialog(clientGui.getPrimaryStage());
        if (selectedFile != null) {
            try {
                client.executeUpload(serverHost, serverPort, selectedFile);
            } catch (IOException e) {
                showInformationDialog(e.getMessage());
                return;
            }
            try {
                client.executeUpdate(serverHost, serverPort);
            } catch (IOException e) {
                showInformationDialog(e.getMessage());
            }
        }
    }

    @FXML
    void handleUpdate() {
        List<SharedFile> sharedFiles;
        availableFilesData.clear();
        try {
            sharedFiles = client.executeList(serverHost, serverPort);
        } catch (IOException e) {
            showInformationDialog(e.getMessage());
            return;
        }
        availableFilesData.addAll(
                sharedFiles.stream()
                        .map(sharedFile -> new AvailableFile(
                                sharedFile.getId(),
                                sharedFile.getSize(),
                                sharedFile.getName()))
                        .collect(Collectors.toList())
        );
    }

    @FXML
    void handleRootDirChooser() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Client Root Dir");
        File defaultDirectory = new File(".");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(clientGui.getPrimaryStage());
        if (selectedDirectory != null) {
            clientRootDirTextField.setText(selectedDirectory.getAbsolutePath());
        } else {
            clientRootDirTextField.setText(defaultDirectory.getAbsolutePath());
        }
    }

    private void showInformationDialog(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("client-icon.png")));
        alert.showAndWait();
    }

    private class ButtonCell extends TableCell<AvailableFile, Boolean> {
        final Button cellButton = new Button("Download");

        ButtonCell(){
            cellButton.setOnAction(t -> {
                AvailableFile availableFile = ButtonCell.this.getTableView().getItems().get(ButtonCell.this.getIndex());
                SharedFile sharedFile = new SharedFile(availableFile.getName(), availableFile.getId(), availableFile.getSize());
                client.addFileToDownloading(serverHost, serverPort, sharedFile);
                showInformationDialog("File\n" + sharedFile + "\nadded to downloading list");
            });
        }
        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if(!empty){
                setGraphic(cellButton);
            } else {
                setGraphic(null);
            }
        }
    }

}
