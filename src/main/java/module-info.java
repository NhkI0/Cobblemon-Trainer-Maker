module com.dopamine.cobblemontrainermaker {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.dopamine.cobblemontrainermaker to javafx.fxml;
    exports com.dopamine.cobblemontrainermaker;
}