package ru.cloudstorage.server.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

public class Network {
    private static final Network ourInstance = new Network();
    private ObjectEncoderOutputStream out;
    private ObjectDecoderInputStream in;

    public static Network getInstance() {
        return ourInstance;
    }

    public ObjectEncoderOutputStream getOut() {
        return out;
    }

    public ObjectDecoderInputStream getIn() {
        return in;
    }

    private Network() {
    }

    private SocketChannel currentChannel;

    public SocketChannel getCurrentChannel() {
        return currentChannel;
    }

    public void start(CountDownLatch countDownLatch) throws IOException {
        try {
            InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8180);
            currentChannel = SocketChannel.open(serverAddress);
            out = new ObjectEncoderOutputStream(currentChannel.socket().getOutputStream());
            in = new ObjectDecoderInputStream(currentChannel.socket().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        countDownLatch.countDown();

    }

    public void stop() throws IOException {
        currentChannel.close();
    }
}
