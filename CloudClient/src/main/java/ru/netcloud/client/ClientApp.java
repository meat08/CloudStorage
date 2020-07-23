package ru.netcloud.client;

import java.io.IOException;

public class ClientApp {

    private static final int SERVER_PORT = 8989;
    private static final String SERVER_NAME = "localhost";

    public static void main(String[] args) {
        int port = getServerPort(args);
        String serverName = getServerName(args);
        try {
            new NetworkClient(serverName, port).connect();
        } catch (IOException e) {
            System.out.println("Ошибка подключения.");
        }
    }

    private static int getServerPort(String[] args) {
        int port = SERVER_PORT;
        if(args.length == 2) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Некорректный формат порта. Использован порт по умолчанию.");
            }
        }
        return port;
    }

    private static String getServerName(String[] args) {
        String serverName = SERVER_NAME;
        if(args.length == 2) {
            serverName = args[1];
        }
        return serverName;
    }
}
