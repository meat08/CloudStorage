package ru.cloudstorage.server.controllers;

import javafx.scene.control.*;
import ru.cloudstorage.clientserver.FileList;
import ru.cloudstorage.clientserver.GetFileListCommand;
import ru.cloudstorage.server.network.Network;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RightPanelController extends PanelsController {

    @Override
    public void updateList(Path path) {
        try {
            Network.getInstance().getOut().writeObject(new GetFileListCommand(path));
            FileList fileList = (FileList) Network.getInstance().getIn().readObject();
            pathField.setText(Paths.get(fileList.getRootPath()).normalize().toString());
            filesTable.getItems().clear();
            filesTable.getItems().addAll(fileList.getFileInfoList());
            filesTable.sort();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @Override
    public void globalUpdateList() {
        updateList(Paths.get(ROOT_PATH.toString(), "client1"));
    }
}
