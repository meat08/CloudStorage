package ru.netcloud.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class ClientHandler {
    private Selector selector;
    private SelectionKey key;
    private Path serverPath;
    private Path clientPath;
    private int clientNum;

    public ClientHandler(Selector selector) {
        this.selector = selector;
    }

    public void waitClient() {
        try {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            if (iterator.hasNext()) {
                key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    System.out.println("Клиент подключился");
                    SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                    createHomeDir();
                }
                if (key.isReadable()) {
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    ((SocketChannel)key.channel()).read(buffer);
                    buffer.flip();
                    StringBuilder s = new StringBuilder();
                    while (buffer.hasRemaining()) {
                        s.append((char)buffer.get());
                    }
                    checkCommand(s.toString());
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Клиент отключился");
            clientNum -= 1;
            try {
                key.channel().close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void createHomeDir() throws IOException {
        clientNum += 1;
        serverPath = Paths.get("CloudServer/data");
        clientPath = Paths.get(serverPath+"/client" +clientNum);
        if (!Files.exists(clientPath)) {
            Files.createDirectory(clientPath);
        }
    }

    private void checkCommand(String s) throws IOException, InterruptedException {
        String[] commandSplit = s.split(" ", 2);
        SocketChannel channel = ((SocketChannel)key.channel());
        Path file = Paths.get(clientPath + "/" +commandSplit[1]);
        if (commandSplit[0].equals("/get")) {
            long size = Files.size(file);
            if (Files.exists(file)) {
                channel.write(ByteBuffer.wrap("OK".getBytes()));
                ByteBuffer bufferLong = ByteBuffer.allocate(Long.BYTES);
                bufferLong.putLong(size);
                bufferLong.flip();
                channel.write(bufferLong);
                channel.write(ByteBuffer.wrap(Files.readAllBytes(file)));
            } else {
                channel.write(ByteBuffer.wrap("FALSE".getBytes()));
            }
        }
        if (commandSplit[0].equals("/put")) {
            channel.write(ByteBuffer.wrap("OK".getBytes()));
            ByteBuffer bufferL = ByteBuffer.allocate(Long.BYTES);
            Thread.sleep(100);
            channel.read(bufferL);
            bufferL.flip();
            StringBuilder sb = new StringBuilder();
            while (bufferL.hasRemaining()) {
                sb.append((char)bufferL.get());
            }
            channel.write(ByteBuffer.wrap("OK".getBytes()));
            Thread.sleep(100);
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            if (buffer.position() < Long.decode(sb.toString()) ) {
                channel.read(buffer);
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                FileChannel fc = new FileOutputStream(file.toFile(), false).getChannel();
                fc.write(buffer);
            }
        }
    }
}
