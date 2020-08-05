package ru.cloudstorage.server.util;

import ru.cloudstorage.clientserver.FileMessage;
import ru.cloudstorage.clientserver.SendFileCommand;
import ru.cloudstorage.server.network.Network;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class FileTransfer {

    public static void putFileToServer(Path src, Path dst, CountDownLatch cdl)  {
        new Thread(() -> {
            try {
                File srcFile = src.toFile();
                int partSend = 0;
                int bufferSize = 1024*1024*10;
                int partsCount = new Long(srcFile.length() / bufferSize).intValue();
                if (srcFile.length() % bufferSize != 0) {
                    partsCount++;
                }
                FileMessage fileMessage = new FileMessage(dst.toString(), -1, partsCount, new byte[bufferSize]);
                FileInputStream in = new FileInputStream(srcFile);
                for (int i = 0; i < partsCount; i++) {
                    int readByte = in.read(fileMessage.data);
                    fileMessage.partNumber = i + 1;
                    if (readByte < bufferSize) {
                        fileMessage.data = Arrays.copyOfRange(fileMessage.data, 0, readByte);
                    }
                    Network.getInstance().getOut().writeObject(fileMessage);
                    partSend++;
                    System.out.println((int)(((float)partSend / partsCount) * 100 ) + "%");
                }
                in.close();
                cdl.countDown();
            } catch (Exception e) {
                e.printStackTrace();
                cdl.countDown();
            }
        }).start();
    }
}
