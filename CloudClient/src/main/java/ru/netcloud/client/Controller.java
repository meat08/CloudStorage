package ru.netcloud.client;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private final byte[] GET_COMMAND = {10};
    private final byte[] PUT_COMMAND = {11};
    private final byte[] OK_COMMAND = {1};

    public ListView<String> lv;
    public TextField txt;
    public Button send;
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private int bufferSize;
    private final String clientFilesPath = "CloudClient/data/clientFiles";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateFileList();
    }

    private void updateFileList() {
        File dir = new File(clientFilesPath);
        lv.getItems().clear();
        for (String file : dir.list()) {
            lv.getItems().add(file);
        }
    }

    public void sendCommand(ActionEvent actionEvent) throws IOException {
        String command = txt.getText();
        String [] op = command.split(" ");
        try {
            if (op[0].equals("/get")) {
                getFileFromServer(op[1]);
                updateFileList();

            } else if (op[0].equals("/put")) {
                putFilesToSever(op[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void putFilesToSever(String s) throws IOException {
        File file = new File(clientFilesPath, s);
        long fileSize = file.length();
        byte[] fileNameBytes = s.getBytes();
        if (fileNameBytes.length > 127) {
            System.out.println("Слишком длинное имя файла");
        } else {
            os.write(PUT_COMMAND);
            if (isOkReceived()) {
                os.write(fileNameBytes.length);
            }
            if (isOkReceived()) {
                os.write(fileNameBytes);
            }
            if (isOkReceived()) {
                os.writeLong(fileSize);
            }
            if (isOkReceived()) {
                try(FileInputStream fos = new FileInputStream(file)) {
                    calculateBufferSize(fileSize);
                    byte [] buffer = new byte[bufferSize];
                    if (fileSize < bufferSize) {
                        int count = fos.read(buffer);
                        os.write(buffer, 0, count);
                    } else {
                        int x = 0;
                        for (long i = 0; i < fileSize / bufferSize; i++) {
                            int count = fos.read(buffer);
                            x += count;
                            System.out.println(x);
                            os.write(buffer, 0, count);
                        }
                    }
                }
            }
        }
    }

    private void getFileFromServer(String s) throws IOException {
        os.write(GET_COMMAND);
        byte[] fileNameBytes = s.getBytes();
        long fileSize;
        if (fileNameBytes.length > 127) {
            System.out.println("Слишком длинное имя файла");
        } else {
            if (isOkReceived()) {
                os.write(fileNameBytes.length);
            }
            if (isOkReceived()) {
                os.write(fileNameBytes);
            }
            if (isOkReceived()) {
                fileSize = is.readLong();
                File file = new File(clientFilesPath, s);
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        System.out.println("не удалось создать файл");
                        return;
                    }
                }
                try(FileOutputStream fos = new FileOutputStream(file)) {
                    calculateBufferSize(fileSize);
                    byte [] buffer = new byte[bufferSize];
                    if (fileSize < bufferSize) {
                        int count = is.read(buffer);
                        fos.write(buffer, 0, count);
                    } else {
                        for (long i = 0; i < fileSize / bufferSize; i++) {
                            int count = is.read(buffer);
                            fos.write(buffer, 0, count);
                        }
                    }
                }
            }
        }
    }

    private void calculateBufferSize(long fileSize) {
        if (fileSize < 1000000) {
            bufferSize = 4096;
        } else {
            bufferSize = 65536;
        }
    }

    private boolean isOkReceived() throws IOException {
        byte[] okBytes = new byte[1];
        is.read(okBytes);
        return Arrays.equals(okBytes, OK_COMMAND);
    }

    private String getServerReady() throws IOException {
        byte[] buf = new byte[1024];
        is.read(buf);
        String response = new String(buf).trim();
        System.out.println("resp: " + response);
        return response;
    }
}
