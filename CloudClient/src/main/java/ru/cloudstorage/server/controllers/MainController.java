package ru.cloudstorage.server.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.cloudstorage.clientserver.AuthorisationCommand;
import ru.cloudstorage.clientserver.FileInfo;
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
    @FXML
    ProgressBar progressBar;

    public void btnExitAction() {
        Network.getInstance().stop();
        Platform.exit();
    }

    public void btnLoginAction() throws Exception {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login.isEmpty() || password.isEmpty()) {
            loginLabel.setText("Заполните все поля!");
        } else {
            AuthorisationCommand command = new AuthorisationCommand(login, password);
            Network.getInstance().getOut().writeObject(command);
            command = (AuthorisationCommand) Network.getInstance().getIn().readObject();
            if (command.isAuthorise() & !command.isLogin()) {
                this.leftPanelController = (LeftPanelController) leftPanel.getProperties().get("ctrl");
                this.rightPanelController = (RightPanelController) rightPanel.getProperties().get("ctrl");
                loginLabel.setText("Авторизован");
                rightPanelController.setServerPaths(command.getRootDir(), command.getClientDir());
                leftPanelController.create();
                rightPanelController.create();
                afterAuthorise();
            } else if (command.isLogin()) {
                loginLabel.setText(command.getMessage());
            } else {
                loginLabel.setText("Неверный логин или пароль");
            }

        }
    }

    public void btnUpdateAction() {
        Path path = Paths.get(rightPanelController.pathField.getText());
        rightPanelController.updateList(path);
        path = Paths.get(leftPanelController.pathField.getText());
        leftPanelController.updateList(path);
    }

    public void btnCopyAction() {
        if (checkPanel()) {
            return;
        }

        Optional<ButtonType> option = Optional.empty();
        for (FileInfo file : dstPC.filesTable.getItems()) {
            if (file.getFilename().equals(srcPath.getFileName().toString())) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Заменить файл в папке назначения?");
                alert.getDialogPane().setHeaderText(null);
                option = alert.showAndWait();
                break;
            }
        }
        if (option.isPresent()) {
            if (option.get() != ButtonType.OK) {
                return;
            }
        }

        waitProcess();
        if (fromClient) {
            FileTransfer.putFileToServer(srcPath, dstPath, progressBar, () -> finishProcess(rightPanelController));
        } else {
            FileTransfer.getFileFromServer(srcPath, dstPath, progressBar, () -> finishProcess(leftPanelController));
        }
    }

    private void waitProcess() {
        buttonBlock.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setManaged(true);
    }

    private void finishProcess(PanelController panel) {
        panel.updateList(dstPath.getParent());
        buttonBlock.setDisable(false);
        progressBar.setVisible(false);
        progressBar.setManaged(false);
    }

    public void btnDeleteAction() {
        if (checkPanel()) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Действительно удалить файл?");
        alert.getDialogPane().setHeaderText(null);
        Optional<ButtonType> option = alert.showAndWait();

        if (option.isPresent()) {
            if (option.get() == ButtonType.OK) {
                if (fromClient) {
                    try {
                        Files.delete(srcPath);
                        leftPanelController.updateList(srcPath.getParent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    FileTransfer.deleteFileFromServer(srcPath);
                    rightPanelController.updateList(srcPath.getParent());
                }
            }
        }
    }

    private boolean checkPanel() {
        if (leftPanelController.getSelectedFilename() == null && rightPanelController.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ни один файл не был выбран", ButtonType.OK);
            alert.getDialogPane().setHeaderText(null);
            alert.showAndWait();
            return true;
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

        if (srcPC.filesTable.getSelectionModel().getSelectedItem().getType() == FileInfo.FileType.DIRECTORY) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Выбрана директория. Выберете файл", ButtonType.OK);
            alert.getDialogPane().setHeaderText(null);
            alert.showAndWait();
            return true;
        }

        srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFilename());
        dstPath = Paths.get(dstPC.getCurrentPath(), srcPath.getFileName().toString());

        return false;
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
