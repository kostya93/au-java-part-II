package TrackerGui;
/**
 * Created by kostya on 01.12.2016.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class TrackerGui extends Application {
    private Stage primaryStage;
    private TrackerGuiController trackerGuiController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("tracker-gui.fxml"));
        Parent root = loader.load();

        trackerGuiController = (TrackerGuiController)loader.getController();
        trackerGuiController.setMainApp(this);

        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("tracker-icon.png")));

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Tracker");
        primaryStage.show();
    }

    Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() throws Exception {
        trackerGuiController.stop();
    }
}
