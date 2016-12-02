package TrackerGui;

import Common.SerializationException;
import Common.SocketIOException;
import Tracker.Tracker;
import Tracker.TrackerImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Created by kostya on 01.12.2016.
 */
public class TrackerGuiController {
    private Tracker tracker = new TrackerImpl();
    private TrackerGui trackerGui;

    @FXML
    private TextField portField;
    @FXML
    private TextField rootDirField;

    @FXML
    private Label statusText;

    @FXML
    private void handleStart() {
        if (tracker.isStarted()) {
            showInformationDialog("server already started");
            return;
        }

        int port;
        if ("".equals(portField.getText())) {
            showInformationDialog("inter port number");
            return;
        }

        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            showInformationDialog("port number must be an integer");
            return;
        }

        if ("".equals(rootDirField.getText())) {
            showInformationDialog("inter root dir");
            return;
        }

        File rootDir = new File(rootDirField.getText());
        rootDir.mkdirs();

        try {
            tracker.start(port, rootDir);
        } catch (Exception e) {
            showInformationDialog(e.getMessage());
            return;
        }
        statusText.setText("ON");
        statusText.setTextFill(Paint.valueOf("GREEN"));
    }

    @FXML
    private void handleStop() {
        if (!tracker.isStarted()) {
            showInformationDialog("server already stoped");
            return;
        }
        try {
            tracker.stop();
        } catch (SocketIOException | SerializationException e) {
            showInformationDialog(e.getMessage());
        }
        statusText.setText("OFF");
        statusText.setTextFill(Paint.valueOf("RED"));
    }

    @FXML
    private void handleRootDirChooser() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Server root dir");
        File defaultDirectory = new File(".");
        chooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = chooser.showDialog(trackerGui.getPrimaryStage());
        if (selectedDirectory != null) {
            rootDirField.setText(selectedDirectory.getAbsolutePath());
        } else {
            rootDirField.setText(defaultDirectory.getAbsolutePath());
        }
    }

    void setMainApp(TrackerGui trackerGui) {
        this.trackerGui = trackerGui;
    }

    private void showInformationDialog(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("tracker-icon.png")));
        alert.showAndWait();
    }
}
