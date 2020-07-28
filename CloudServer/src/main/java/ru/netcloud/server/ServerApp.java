package ru.netcloud.server;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

public class ServerApp implements Runnable {
    private final byte[] GET_COMMAND = {10};
    private final byte[] PUT_COMMAND = {11};
    private final byte[] OK_COMMAND = {1};
    private final Path clientPath = Paths.get("CloudServer", "data");
    private ServerSocketChannel server;
    private Selector selector;
    private ByteBuffer buf = ByteBuffer.allocate(256);
    private ByteBuffer bigBuf = ByteBuffer.allocate(65536);
    private int clientIndex;

    public ServerApp() throws IOException {
        this.server = ServerSocketChannel.open();
        this.server.socket().bind(new InetSocketAddress(8189));
        this.server.configureBlocking(false);
        this.selector = Selector.open();
        this.server.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        try {
            System.out.println("Сервер запущен на порту 8189");
            Iterator<SelectionKey> iter;
            SelectionKey key;
            while (server.isOpen()) {
                selector.select();
                iter = this.selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    key = iter.next();
                    iter.remove();
                    if (key.isAcceptable()) this.handleAccept(key);
                    if (key.isReadable()) this.handleRead(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel serverChannel = ((ServerSocketChannel) key.channel()).accept();
        clientIndex++;
        String clientName = "client" + clientIndex;
        createHomeDir(clientName);
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_READ, clientName);
        System.out.println("Подключился новый клиент " + clientName);
    }

    private void handleRead(SelectionKey key) throws IOException {
        buf.clear();
        SocketChannel clientChannel = (SocketChannel) key.channel();
        try {
            while (clientChannel.read(buf) > 0) {
                buf.flip();
                byte[] bytes = new byte[1];
                buf.get(bytes);
                if (Arrays.equals(bytes, GET_COMMAND)) {
                    putFileToClient(clientChannel, key);
                }
                if (Arrays.equals(bytes, PUT_COMMAND)) {
                    getFileFromClient(clientChannel, key);
                }
                buf.clear();
            }
        } catch (IOException e) {
            System.out.println(key.attachment() + " отключился");
            clientIndex--;
            clientChannel.close();
        }
    }

    private void getFileFromClient(SocketChannel channel, SelectionKey key) throws IOException {
        System.out.println("put success");
        buf.clear();
        sendOkCommand(channel);
        String fileName = readFileName(channel);
        Path filePath = Paths.get(clientPath.toString(), key.attachment().toString(), fileName);
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
        sendOkCommand(channel);
        long size = bufToLong(channel);
        sendOkCommand(channel);
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toString(), "rw")){
            FileChannel inFile = raf.getChannel();
            bigBuf.clear();
            long bytesRead = 0;
            while (bytesRead < size) {
                bytesRead += channel.read(bigBuf);
                bigBuf.flip();
                inFile.write(bigBuf);
                bigBuf.flip();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            buf.clear();
            bigBuf.clear();
            inFile.close();
        }
    }

    private void putFileToClient(SocketChannel channel, SelectionKey key) throws IOException {
        System.out.println("get success");
        buf.clear();
        sendOkCommand(channel);
        String fileName = readFileName(channel);
        Path filePath = Paths.get(clientPath.toString(), key.attachment().toString(), fileName);
        long size = Files.size(filePath);
        sendOkCommand(channel);
        channel.write(ByteBuffer.wrap(longToBytes(size)));
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toString(), "r")){
            FileChannel outFile = raf.getChannel();
            bigBuf.clear();
            long bytesRead = 0;
            while (bytesRead < size) {
                bytesRead += outFile.read(bigBuf);
                bigBuf.flip();
                channel.write(bigBuf);
                bigBuf.flip();
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            buf.clear();
            outFile.close();
        }

    }

    private void sendOkCommand(SocketChannel channel) throws IOException {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        channel.write(ByteBuffer.wrap(OK_COMMAND));
    }

    private int readFileNameSize(SocketChannel channel) throws IOException {
        int fileNameSize = 0;
        while (buf.position() < 1) {
            if(channel.read(buf) > 0) {
                buf.flip();
                fileNameSize = buf.get();
            }
        }
        buf.clear();
        return fileNameSize;
    }

    private String readFileName(SocketChannel channel) throws IOException {
        StringBuilder sb = new StringBuilder();
        int fileNameSize = readFileNameSize(channel);
        sendOkCommand(channel);
        while (buf.position() < fileNameSize) {
            if(channel.read(buf) > 0) {
                buf.flip();
                while (buf.hasRemaining()) {
                    sb.append((char)buf.get());
                }
            }
        }
        buf.clear();
        return sb.toString();
    }

    private byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    private long bufToLong(SocketChannel channel) throws IOException {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int count = 0;
        while (count < Long.BYTES) {
            count += channel.read(buf);
            buf.flip();
        }
        return buf.getLong();
    }

    private void createHomeDir(String clientName) throws IOException {
        Path path = Paths.get(clientPath.toString(), clientName);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
    }


    public static void main(String[] args) throws IOException {
        new Thread(new ServerApp()).start();
    }
}
