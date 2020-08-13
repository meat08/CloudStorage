package ru.cloudstorage.clientserver;

import java.io.Serializable;

public class RemoveFileCommand extends Command {
    private String filePath;

    public RemoveFileCommand(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
