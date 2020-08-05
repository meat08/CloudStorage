package ru.cloudstorage.server;

import ru.cloudstorage.server.util.ServerProperties;

public class CloudServer {
    private static final int DEFAULT_PORT = 8180;

    private static int getServerPort(String[] args) {
        try {
            return new ServerProperties().getPort();
        } catch (Exception e) {
            System.out.println("Файл конфигурации недоступен. Использован порт по умолчанию.");
            return DEFAULT_PORT;
        }
    }


    public static void main(String[] args) throws Exception {
        int port = getServerPort(args);
        new NetworkServer(port).run();
    }
}
