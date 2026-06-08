module com.pdv {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.pdv.app        to javafx.graphics;
    opens com.pdv.controller to javafx.fxml;
    opens com.pdv.model      to javafx.base;

    exports com.pdv.app;
}
