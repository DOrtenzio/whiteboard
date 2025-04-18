package whiteboard.whiteboard.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import whiteboard.whiteboard.azioni.Stato;

import java.io.IOException;

public class ClientController {
    @FXML
    private AnchorPane anchorApp;

    private Client client;

    @FXML
    public void initialize(){
        TextField idLett = new TextField();
        Button button = new Button("Invia");

        // Posizionamento
        AnchorPane.setTopAnchor(idLett, 50.0);
        AnchorPane.setLeftAnchor(idLett, 50.0);

        AnchorPane.setTopAnchor(button, 100.0);
        AnchorPane.setLeftAnchor(button, 50.0);

        button.setOnMouseClicked(e -> {
            this.client = new Client(idLett.getText(), this);
            anchorApp.getChildren().clear();
            faseSeconda();
        });

        anchorApp.getChildren().addAll(idLett, button);
    }


    private void startClient(String nomeLavagna, String idLavagna){ new Thread(() -> client.run(nomeLavagna,idLavagna)).start(); }

    private void faseSeconda() {
        Button crea = new Button("CREA");
        Button accedi = new Button("ACCEDI");

        AnchorPane.setTopAnchor(crea, 50.0);
        AnchorPane.setLeftAnchor(crea, 50.0);

        AnchorPane.setTopAnchor(accedi, 100.0);
        AnchorPane.setLeftAnchor(accedi, 50.0);

        crea.setOnMouseClicked(e -> {
            anchorApp.getChildren().clear();
            TextField idLett = new TextField();
            Button button = new Button("Crea");

            AnchorPane.setTopAnchor(idLett, 50.0);
            AnchorPane.setLeftAnchor(idLett, 50.0);

            AnchorPane.setTopAnchor(button, 100.0);
            AnchorPane.setLeftAnchor(button, 50.0);

            button.setOnMouseClicked(ei -> {
                startClient(idLett.getText(), null);
            });

            anchorApp.getChildren().addAll(idLett, button);
        });

        accedi.setOnMouseClicked(e -> {
            anchorApp.getChildren().clear();
            TextField idLavagnaLetto = new TextField();
            Button button = new Button("Accedi");

            AnchorPane.setTopAnchor(idLavagnaLetto, 50.0);
            AnchorPane.setLeftAnchor(idLavagnaLetto, 50.0);

            AnchorPane.setTopAnchor(button, 100.0);
            AnchorPane.setLeftAnchor(button, 50.0);

            button.setOnMouseClicked(ei -> {
                startClient(null, idLavagnaLetto.getText());
            });

            anchorApp.getChildren().addAll(idLavagnaLetto, button);
        });

        anchorApp.getChildren().addAll(crea, accedi);
    }

    @FXML
    public LavagnaController cambiaLavagnaView(String lavagnaNome, Stato statoLavagna) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/whiteboard/whiteboard/lavagna-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1178, 785);
        Stage stage = (Stage) anchorApp.getScene().getWindow();
        stage.setResizable(false);
        stage.getIcons().add(new Image(HelloApplication.class.getResource("/whiteboard/whiteboard/img/logo.png").toString()));
        stage.setScene(scene);

        LavagnaController lavagnaController = fxmlLoader.getController();
        lavagnaController.setLavagnaNome(lavagnaNome); //Imposto il nome
        lavagnaController.setStatoLavagna(statoLavagna); //Disegno lo stato della lavagna che ricordiamo può essere vuoto

        return lavagnaController;
    }

}
