package ru.netcloud.clientserver.command;

import java.io.File;
import java.io.Serializable;

public class ListCommand implements Serializable {
    private File dir;
    private File[] fileList;

    public ListCommand(File[] fileList) {
        this.fileList = fileList;
    }

    public ListCommand(File dir) {
        this.dir = dir;
    }

    public File[] getFileList() {
        return fileList;
    }

    public File getDir() {
        return dir;
    }
}
