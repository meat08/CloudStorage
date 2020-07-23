package ru.netcloud.clientserver.command;

import java.io.File;
import java.io.Serializable;

public class PutGetCommand implements Serializable {
    private final File file;

    public PutGetCommand(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
