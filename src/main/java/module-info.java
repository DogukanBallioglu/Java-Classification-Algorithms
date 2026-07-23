module com.example.prolabii {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens com.example.prolabii to javafx.fxml;
    exports com.example.prolabii;
}