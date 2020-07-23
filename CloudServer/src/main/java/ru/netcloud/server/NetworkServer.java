package ru.netcloud.server;

import ru.netcloud.server.clients.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkServer {

    private final int port;
    private static int clientNum;

    public NetworkServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);
            while (true) {
                System.out.println("Ожидание подключения клиента.");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключился.");
                createClientHandler(clientSocket);

            }
        } catch (IOException e) {
            System.out.println("Ошибка сервера");
        }
    }

    private void createClientHandler(Socket clientSocket) {
        clientNum += 1;
        ClientHandler clientHandler = new ClientHandler(clientSocket, clientNum);
        clientHandler.run();
    }

    public static void decreaseClientNum() {
        clientNum -= 1;
    }

}
