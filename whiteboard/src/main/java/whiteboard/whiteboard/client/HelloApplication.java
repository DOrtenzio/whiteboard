package whiteboard.whiteboard.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/whiteboard/whiteboard/client-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1178, 785);
        stage.setTitle("SketchIt");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResource("/whiteboard/whiteboard/img/logo.png").toString()));
        stage.show();

        ClientController clientController=fxmlLoader.getController();
        clientController.firstInitialize();
        stage.setOnCloseRequest(event -> {
            // Mostra alert di conferma uscita così che riesca a chiudere in modo corretto gli stream
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Sei sicuro di voler uscire?");
            alert.setTitle("Conferma uscita");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                event.consume();
            }else{
                clientController.getClient().chiudiConnessione();
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}