package ru.cloudstorage.server.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ru.cloudstorage.clientserver.FileListCommand;
import ru.cloudstorage.clientserver.GetFileListCommand;
import ru.cloudstorage.server.network.Network;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class RightPanelController extends PanelController {

    private String serverRootPath;
    private String serverClientPath;

    @Override
    public void globalUpdateList() {
        updateList(Paths.get(serverClientPath));
    }

    @Override
    public void updateList(Path path) {
        try {
            Network.getInstance().getOut().writeObject(new GetFileListCommand(path));
            FileListCommand fileList = (FileListCommand) Network.getInstance().getIn().readObject();
            pathField.setText(Paths.get(fileList.getRootPath()).normalize().toString());
            filesTable.getItems().clear();
            filesTable.getItems().addAll(fileList.getFileInfoList());
            filesTable.sort();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnPathUpActionR(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null & !Objects.equals(upperPath, Paths.get(serverRootPath))) {
            updateList(upperPath);
        }
    }

    public void setServerPaths(String serverRootPath, String serverClientPath) {
        this.serverRootPath = serverRootPath;
        this.serverClientPath = serverClientPath;
    }
}
