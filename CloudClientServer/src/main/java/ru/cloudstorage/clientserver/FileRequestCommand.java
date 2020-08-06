package ru.cloudstorage.clientserver;

import java.io.Serializable;

public class FileRequestCommand implements Serializable {

    private String filePath;

    public FileRequestCommand(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
