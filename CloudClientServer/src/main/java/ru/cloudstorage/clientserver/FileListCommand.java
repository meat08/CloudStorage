package ru.cloudstorage.clientserver;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FileListCommand implements Serializable {

    private List<FileInfo> fileInfoList;
    private String rootPath;

    public void formFileInfoList(Path rootPath) {
        this.rootPath = rootPath.toString();
        try {
            fileInfoList = Files.list(rootPath).map(FileInfo::new).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRootPath() {
        return rootPath;
    }

    public List<FileInfo> getFileInfoList() {
        return fileInfoList;
    }
}
