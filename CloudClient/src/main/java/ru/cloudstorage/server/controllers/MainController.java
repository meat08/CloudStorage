package ru.cloudstorage.server.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import ru.cloudstorage.server.network.Network;

import java.io.IOException;

public class MainController {
    @FXML
    VBox leftPanel, rightPanel;

    public void btnExitAction(ActionEvent actionEvent) throws IOException {
        Network.getInstance().stop();
        Platform.exit();
    }
}
