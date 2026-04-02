module com.example.projectpoker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.example.projectpoker to javafx.fxml;
    exports com.example.projectpoker;
    exports com.example.projectpoker.controller;
    opens com.example.projectpoker.controller to javafx.fxml;
    exports com.example.projectpoker.model;
    opens com.example.projectpoker.model to javafx.fxml;
    exports com.example.projectpoker.model.game;
    opens com.example.projectpoker.model.game to javafx.fxml;
    exports com.example.projectpoker.model.game.statemachine;
    opens com.example.projectpoker.model.game.statemachine to javafx.fxml;
    exports com.example.projectpoker.model.game.oberserver;
    opens com.example.projectpoker.model.game.oberserver to javafx.fxml;
    exports com.example.projectpoker.model.game.enums;
    opens com.example.projectpoker.model.game.enums to javafx.fxml;
}