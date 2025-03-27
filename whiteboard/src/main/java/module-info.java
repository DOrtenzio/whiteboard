module whiteboard.whiteboard {
    requires javafx.controls;
    requires javafx.fxml;


    opens whiteboard.whiteboard to javafx.fxml;
    exports whiteboard.whiteboard;
}