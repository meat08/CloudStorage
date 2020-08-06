package ru.cloudstorage.clientserver;

import java.io.Serializable;

public class FileMessageCommand implements Serializable {

    public String dstFilePath;
    public int partNumber;
    public int partCount;
    public byte[] data;

    public FileMessageCommand(String dstFilePath, int partNumber, int partCount, byte[] data) {
        this.dstFilePath = dstFilePath;
        this.partNumber = partNumber;
        this.partCount = partCount;
        this.data = data;
    }
}
