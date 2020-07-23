package ru.netcloud.server;

public class ServerApp {

    private static final int DEFAULT_PORT = 8989;

    public static void main(String[] args) {
        int port = getServerPort(args);
        new NetworkServer(port).start();
    }

    private static int getServerPort(String[] args) {
        int port = DEFAULT_PORT;
        if(args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Некорректный формат порта. Использован порт по умолчанию.");
            }
        }
        return port;
    }
}
