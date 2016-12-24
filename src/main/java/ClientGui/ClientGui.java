package ClientGui;
/**
 * Created by kostya on 02.12.2016.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientGui extends Application {
    private Stage primaryStage;
    private ClientGuiController clientGuiController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("client-gui.fxml"));
        Parent root = loader.load();

        clientGuiController = ((ClientGuiController)loader.getController());
        clientGuiController.setMainApp(this);

        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("client-icon.png")));

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Client");
        primaryStage.show();
    }

    Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() throws Exception {
        clientGuiController.stop();
    }
}
