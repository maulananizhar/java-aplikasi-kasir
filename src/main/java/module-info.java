module com.kasir {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires atlantafx.base;
    requires transitive org.apache.pdfbox;
    requires java.desktop;

    opens com.kasir.services to javafx.base;
    opens com.kasir to javafx.fxml;
    opens com.kasir.model to javafx.base;
    opens com.kasir.libs to javafx.base;

    exports com.kasir.services;
    exports com.kasir;
    exports com.kasir.model;
    exports com.kasir.libs;
}
