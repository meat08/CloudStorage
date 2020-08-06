package ru.cloudstorage.clientserver;

import java.io.Serializable;

public class RemoveFileCommand implements Serializable {
    private String filePath;

    public RemoveFileCommand(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
