module lk.ijse.etecmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens lk.ijse.etecmanagementsystem to javafx.fxml;
    exports lk.ijse.etecmanagementsystem;

}