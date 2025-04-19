package whiteboard.whiteboard.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import whiteboard.whiteboard.azioni.LogsLavagne;
import whiteboard.whiteboard.azioni.Stato;

import java.io.IOException;

public class ClientController {
    @FXML
    private AnchorPane anchorBase;
    @FXML
    private Label labelBase;

    private Client client;

    @FXML
    public void initialize(){
        //Creazioni elementi UI
        TextField textField = new TextField();
        textField.setLayoutX(340.0);
        textField.setLayoutY(56.0);
        textField.setPrefWidth(356.0);
        textField.setPrefHeight(51.0);
        textField.setStyle("-fx-border-color: E9EEF4; " +
                "-fx-border-width: 1.2; " +
                "-fx-background-color: white; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12;");

        Button button = new Button("Accedi");
        button.setLayoutX(569.0);
        button.setLayoutY(151.0);
        button.setPrefWidth(119.0);
        button.setPrefHeight(42.0);
        button.setDefaultButton(true);
        button.setMnemonicParsing(false);
        button.setStyle("-fx-background-color: E9EEF4; " +
                "-fx-border-color: E9EEF4; " +
                "-fx-border-radius: 12px; " +
                "-fx-background-radius: 12px;");
        button.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
        button.setOnMouseMoved(event -> button.setStyle("-fx-background-color: grey; " +
                "-fx-border-color: E9EEF4; " +
                "-fx-border-radius: 12px; " +
                "-fx-background-radius: 12px;"));
        button.setOnMouseExited(event -> button.setStyle("-fx-background-color: E9EEF4; " +
                "-fx-border-color: E9EEF4; " +
                "-fx-border-radius: 12px; " +
                "-fx-background-radius: 12px;"));

        button.setOnMouseClicked(e -> {
            this.client = new Client(textField.getText(), this);
            labelBase.setText("Benvenuto/a "+client.getNomeUtente()+"! Seleziona una board o creala");
            anchorBase.getChildren().clear();
            postAccesso();
        });

        anchorBase.getChildren().addAll(textField, button);
    }


    private void startClient(String nomeLavagna, String idLavagna){ new Thread(() -> client.run(nomeLavagna,idLavagna)).start(); }

    private void postAccesso() {
        anchorBase.getChildren().clear();
        LogsLavagne lgv = client.firstRun(); //Richiedo l'avvio della connessione e la richiesta al server centrale delle info sulle mie lavagne
        creaGrigliaHome(anchorBase,lgv);
    }

    //Metodo per la creazione dinamica dei bottoni
    private void creaGrigliaHome(AnchorPane anchorBase, LogsLavagne lgv) {
        // Lista di parametri per ciascun bottone (layoutX, layoutY, testo)
        Object[][] bottoni = {
                {93.0,   33.0, "+"},
                {378.5,  33.0, "Aggiungi con codice"},
                {664.0,  31.0, "..."},
                {93.0,  228.0, lgv.getSingolaLavagna(0)},
                {378.5, 228.0, lgv.getSingolaLavagna(1)},
                {664.0, 226.0, lgv.getSingolaLavagna(2)},
                {93.0,  423.0, lgv.getSingolaLavagna(3)},
                {379.0, 423.0, lgv.getSingolaLavagna(4)},
                {664.0, 423.0, lgv.getSingolaLavagna(5)}
        };

        for (Object[] params : bottoni) {
            Button button = new Button(((String) params[2]).split("£")[0]); //isolo solo il nome
            button.setLayoutX((double) params[0]);
            button.setLayoutY((double) params[1]);
            button.setPrefWidth(239.0);
            button.setPrefHeight(151.0);
            button.setMnemonicParsing(false);
            button.setWrapText(true);
            button.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            button.setFont(new Font(24));
            button.setStyle(
                    "-fx-border-color: E9EEF4; " +
                            "-fx-border-width: 1.5px; " +
                            "-fx-border-radius: 12px; " +
                            "-fx-background-radius: 12px; " +
                            "-fx-background-color: white;"
            );
            //Modifiche per casi speciali
            if (button.getText().equalsIgnoreCase("+")){
                button.setOnMouseClicked(e -> {
                    anchorBase.getChildren().clear();
                    labelBase.setText("Dai "+client.getNomeUtente()+", crea una nuova Lavagna !");

                    TextField textField = new TextField();
                    textField.setLayoutX(340.0);
                    textField.setLayoutY(56.0);
                    textField.setPrefWidth(356.0);
                    textField.setPrefHeight(51.0);
                    textField.setStyle("-fx-border-color: E9EEF4; " +
                            "-fx-border-width: 1.2; " +
                            "-fx-background-color: white; " +
                            "-fx-border-radius: 12; " +
                            "-fx-background-radius: 12;");

                    Button buttonI = new Button("Crea");
                    buttonI.setLayoutX(569.0);
                    buttonI.setLayoutY(151.0);
                    buttonI.setPrefWidth(119.0);
                    buttonI.setPrefHeight(42.0);
                    buttonI.setDefaultButton(true);
                    buttonI.setMnemonicParsing(false);
                    buttonI.setStyle("-fx-background-color: E9EEF4; " +
                            "-fx-border-color: E9EEF4; " +
                            "-fx-border-radius: 12px; " +
                            "-fx-background-radius: 12px;");
                    buttonI.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
                    buttonI.setOnMouseMoved(event -> buttonI.setStyle("-fx-background-color: grey; " +
                            "-fx-border-color: E9EEF4; " +
                            "-fx-border-radius: 12px; " +
                            "-fx-background-radius: 12px;"));
                    buttonI.setOnMouseExited(event -> buttonI.setStyle("-fx-background-color: E9EEF4; " +
                            "-fx-border-color: E9EEF4; " +
                            "-fx-border-radius: 12px; " +
                            "-fx-background-radius: 12px;"));

                    buttonI.setOnMouseClicked(ei -> {
                        startClient(textField.getText(), null);
                    });

                    anchorBase.getChildren().addAll(textField, buttonI);

                });
            } else if (button.getText().equalsIgnoreCase("Aggiungi con codice")) {
                button.setOnMouseClicked(e -> {
                    anchorBase.getChildren().clear();
                    labelBase.setText("Ora "+client.getNomeUtente()+"devi inserire il codice condivisoti !");

                    TextField textField = new TextField();
                    textField.setLayoutX(340.0);
                    textField.setLayoutY(56.0);
                    textField.setPrefWidth(356.0);
                    textField.setPrefHeight(51.0);
                    textField.setStyle("-fx-border-color: E9EEF4; " +
                            "-fx-border-width: 1.2; " +
                            "-fx-background-color: white; " +
                            "-fx-border-radius: 12; " +
                            "-fx-background-radius: 12;");

                    Button buttonI = new Button("Accedi");
                    buttonI.setLayoutX(569.0);
                    buttonI.setLayoutY(151.0);
                    buttonI.setPrefWidth(119.0);
                    buttonI.setPrefHeight(42.0);
                    buttonI.setDefaultButton(true);
                    buttonI.setMnemonicParsing(false);
                    buttonI.setStyle("-fx-background-color: E9EEF4; " +
                            "-fx-border-color: E9EEF4; " +
                            "-fx-border-radius: 12px; " +
                            "-fx-background-radius: 12px;");
                    buttonI.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
                    buttonI.setOnMouseMoved(event -> buttonI.setStyle("-fx-background-color: grey; " +
                            "-fx-border-color: E9EEF4; " +
                            "-fx-border-radius: 12px; " +
                            "-fx-background-radius: 12px;"));
                    buttonI.setOnMouseExited(event -> buttonI.setStyle("-fx-background-color: E9EEF4; " +
                            "-fx-border-color: E9EEF4; " +
                            "-fx-border-radius: 12px; " +
                            "-fx-background-radius: 12px;"));

                    buttonI.setOnMouseClicked(ei -> {
                        double x = button.getLayoutX();
                        double y = button.getLayoutY();
                        String idLavagna = null;

                        // Cerca nella matrice il bottone con la stessa posizione
                        for (Object[] b : bottoni) {
                            if ((double) b[0] == x && (double) b[1] == y) {
                                idLavagna = (String) b[2];
                                break;
                            }
                        }

                        startClient(null, idLavagna);
                    });

                    anchorBase.getChildren().addAll(textField, buttonI);
                });
            } else{
                button.setOnMouseClicked(ei -> {
                    if (!button.getText().equalsIgnoreCase("NOT_FOUND")) startClient(null,button.getText());
                });
            }
            anchorBase.getChildren().add(button);
        }

        // Label "Ecco le tue ultime lavagne"
        Label label = new Label("Ecco le tue ultime lavagne");
        label.setLayoutX(387.0);
        label.setLayoutY(194.0);
        label.setPrefWidth(226.0);
        label.setPrefHeight(27.0);
        label.setFont(new Font(24));
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        anchorBase.getChildren().add(label);
    }

    @FXML
    public LavagnaController cambiaLavagnaView(String lavagnaNome, Stato statoLavagna) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/whiteboard/whiteboard/lavagna-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1178, 785);
        Stage stage = (Stage) anchorBase.getScene().getWindow();
        stage.setResizable(false);
        stage.getIcons().add(new Image(HelloApplication.class.getResource("/whiteboard/whiteboard/img/logo.png").toString()));
        stage.setScene(scene);

        LavagnaController lavagnaController = fxmlLoader.getController();
        lavagnaController.setLavagnaNome(lavagnaNome); //Imposto il nome
        lavagnaController.setStatoLavagna(statoLavagna); //Disegno lo stato della lavagna che ricordiamo può essere vuoto

        return lavagnaController;
    }

}
