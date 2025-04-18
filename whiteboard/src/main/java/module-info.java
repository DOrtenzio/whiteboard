module whiteboard.whiteboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;

    exports whiteboard.whiteboard.azioni;
    opens whiteboard.whiteboard.azioni to javafx.fxml, com.fasterxml.jackson.databind;

    exports whiteboard.whiteboard.azioni.figure;
    opens whiteboard.whiteboard.azioni.figure to com.fasterxml.jackson.databind;
    exports whiteboard.whiteboard.client;
    opens whiteboard.whiteboard.client to javafx.fxml;
    exports whiteboard.whiteboard.server;
    opens whiteboard.whiteboard.server to javafx.fxml;

}
