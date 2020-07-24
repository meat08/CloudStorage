package ru.netcloud.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class ServerApp {
    private ServerSocketChannel server;
    private Selector selector;
    private ClientHandler clientHandler;

    public ServerApp() throws IOException {
        selector = Selector.open();
        server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        clientHandler = new ClientHandler(selector);
    }

    public void start() {
        System.out.println("Сервер запущен");
        while (server.isOpen()) {
            clientHandler.waitClient();
        }
    }

    public static void main(String[] args) {
        try {
            new ServerApp().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
