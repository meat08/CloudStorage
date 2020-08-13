package ru.cloudstorage.client.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import ru.cloudstorage.clientserver.FileMessageCommand;
import ru.cloudstorage.clientserver.FileRequestCommand;
import ru.cloudstorage.clientserver.RemoveFileCommand;
import ru.cloudstorage.client.network.Network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public class FileTransfer {

    public static void putFileToServer(Path src, Path dst, ProgressBar progressBar, WaitCallback callback, ErrorCallback error)  {
        new Thread(() -> {
            try {
                File srcFile = src.toFile();
                int partSend = 0;
                int bufferSize = 1024*1024*10;
                int partsCount = new Long(srcFile.length() / bufferSize).intValue();
                if (srcFile.length() % bufferSize != 0) {
                    partsCount++;
                }
                FileMessageCommand fileMessage = new FileMessageCommand(dst.toString(), -1, partsCount, new byte[bufferSize]);
                FileInputStream in = new FileInputStream(srcFile);
                if (partsCount == 0) {
                    fileMessage.partNumber = 0;
                    fileMessage.partCount = 0;
                    Network.getInstance().getOut().writeObject(fileMessage);
                    nullFileSizeAlert();
                }
                for (int i = 0; i < partsCount; i++) {
                    int readByte = in.read(fileMessage.data);
                    fileMessage.partNumber = i + 1;
                    if (readByte < bufferSize) {
                        fileMessage.data = Arrays.copyOfRange(fileMessage.data, 0, readByte);
                    }
                    Network.getInstance().getOut().writeObject(fileMessage);
                    partSend++;
                    int finalPartSend = partSend;
                    int finalPartsCount = partsCount;
                    Platform.runLater(() -> progressBar.setProgress((((float) finalPartSend / finalPartsCount))));
                }
                in.close();
                callback.callback();
            } catch (Exception e) {
                Platform.runLater(error::error);
            }
        }).start();
    }

    public static void getFileFromServer(Path src, Path dst, ProgressBar progressBar, WaitCallback callback, ErrorCallback error) {
        try {
            Network.getInstance().getOut().writeObject(new FileRequestCommand(src.toString()));
        } catch (IOException e) {
            error.error();
            return;
        }
        new Thread(() -> {
            try {
                while (true) {
                    Object input = Network.getInstance().getIn().readObject();
                    FileMessageCommand fileMessage = (FileMessageCommand) input;
                    if (fileMessage.partCount == 0) {
                        callback.callback();
                        nullFileSizeAlert();
                        break;
                    }
                    boolean append = true;
                    if (fileMessage.partNumber == 1) {
                        append = false;
                    }
                    FileOutputStream fos = new FileOutputStream(dst.toString(), append);
                    fos.write(fileMessage.data);
                    fos.close();
                    Platform.runLater(() -> progressBar.setProgress((((float)fileMessage.partNumber / fileMessage.partCount))));
                    if (fileMessage.partNumber == fileMessage.partCount) {
                        callback.callback();
                        break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void deleteFileFromServer(Path path) {
        try {
            Network.getInstance().getOut().writeObject(new RemoveFileCommand(path.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void nullFileSizeAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Размер файла должен быть больше 0!", ButtonType.OK);
            alert.getDialogPane().setHeaderText(null);
            alert.showAndWait();
        });
    }
}
