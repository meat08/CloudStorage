package ru.cloudstorage.server.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.cloudstorage.clientserver.AuthorisationCommand;
import ru.cloudstorage.server.network.Network;
import ru.cloudstorage.server.util.FileTransfer;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class MainController {

    private LeftPanelController leftPanelController;
    private RightPanelController rightPanelController;
    private PanelController srcPC = null, dstPC = null;
    private boolean fromClient;
    private Path srcPath, dstPath;

    @FXML
    VBox leftPanel, rightPanel;
    @FXML
    HBox buttonBlock;
    @FXML
    VBox loginBox;
    @FXML
    HBox tablePanel;
    @FXML
    Label loginLabel;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;

    public void btnExitAction(ActionEvent actionEvent) {
        Network.getInstance().stop();
        Platform.exit();
    }

    public void btnLoginAction(ActionEvent actionEvent) throws Exception {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login.isEmpty() || password.isEmpty()) {
            loginLabel.setText("Заполните все поля!");
        } else {
            AuthorisationCommand command = new AuthorisationCommand(login, password);
            Network.getInstance().getOut().writeObject(command);
            command = (AuthorisationCommand) Network.getInstance().getIn().readObject();
            if (command.isAuthorise()) {
                this.leftPanelController = (LeftPanelController) leftPanel.getProperties().get("ctrl");
                this.rightPanelController = (RightPanelController) rightPanel.getProperties().get("ctrl");
                loginLabel.setText("Авторизован");
                rightPanelController.setServerPaths(command.getRootDir(), command.getClientDir());
                leftPanelController.create();
                rightPanelController.create();
                afterAuthorise();
            } else {
                loginLabel.setText("Неверный логин или пароль");
            }

        }
    }

    public void btnUpdateAction(ActionEvent actionEvent) {
        Path path = Paths.get(rightPanelController.pathField.getText());
        rightPanelController.updateList(path);
    }

    public void btnCopyAction(ActionEvent actionEvent) {
        checkPanel();

        CountDownLatch cdl = new CountDownLatch(1);
        if (fromClient) {
            FileTransfer.putFileToServer(srcPath, dstPath, cdl);
            buttonBlock.setDisable(true);
                new Thread(() -> {
                    try {
                        cdl.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    rightPanelController.updateList(dstPath.getParent());
                    buttonBlock.setDisable(false);
                }).start();
        }
    }

    public void btnDeleteAction(ActionEvent actionEvent) {
        checkPanel();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Действительно удалить файл?");
        Optional<ButtonType> option = alert.showAndWait();

        if (option.get() == ButtonType.OK) {
            if (fromClient) {
                try {
                    Files.delete(dstPath);
                    leftPanelController.updateList(srcPath.getParent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
            }
        }
    }

    private void checkPanel() {
        if (leftPanelController.getSelectedFilename() == null && rightPanelController.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ни один файл не был выбран", ButtonType.OK);
            alert.getDialogPane().setHeaderText(null);
            alert.showAndWait();
            return;
        }

        if (leftPanelController.getSelectedFilename() != null) {
            srcPC = leftPanelController;
            dstPC = rightPanelController;
            fromClient = true;
        }
        if (rightPanelController.getSelectedFilename() != null) {
            srcPC = rightPanelController;
            dstPC = leftPanelController;
            fromClient = false;
        }

        srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFilename());
        dstPath = Paths.get(dstPC.getCurrentPath(), srcPath.getFileName().toString());
    }

    private void afterAuthorise() {
        loginBox.setVisible(false);
        loginBox.setManaged(false);
        tablePanel.setVisible(true);
        tablePanel.setManaged(true);
        buttonBlock.setVisible(true);
        buttonBlock.setManaged(true);
    }
}
