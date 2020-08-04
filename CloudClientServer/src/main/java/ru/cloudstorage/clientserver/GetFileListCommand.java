package ru.cloudstorage.clientserver;

import java.io.Serializable;
import java.nio.file.Path;

public class GetFileListCommand implements Serializable {
    private String path;

    public GetFileListCommand(Path path) {
        this.path = path.toString();
    }

    public String getPath() {
        return path;
    }
}
