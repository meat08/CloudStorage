package ru.cloudstorage.server.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import ru.cloudstorage.server.util.ClientProperties;

import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

public class Network {
    private static final Network ourInstance = new Network();
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8180;
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
            ClientProperties properties = new ClientProperties();
            String host = properties.getHost();
            int port = properties.getPort();
            if (host == null) {
                System.out.println("Файл конфигурации недоступен. Использован хост по умолчанию.");
                host = DEFAULT_HOST;
            }
            if (port == 0) {
                System.out.println("Файл конфигурации недоступен. Использован порт по умолчанию.");
                port = DEFAULT_PORT;
            }
            InetSocketAddress serverAddress = new InetSocketAddress(host, port);
            currentChannel = SocketChannel.open(serverAddress);
            out = new ObjectEncoderOutputStream(currentChannel.socket().getOutputStream());
            in = new ObjectDecoderInputStream(currentChannel.socket().getInputStream(), 100 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
        countDownLatch.countDown();

    }

    public void stop() {
        try {
            in.close();
            out.close();
            currentChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
