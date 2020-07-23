package ru.netcloud.client;

import ru.netcloud.clientserver.Command;
import ru.netcloud.clientserver.command.HomeCommand;
import ru.netcloud.clientserver.command.ListCommand;

import java.io.*;
import java.net.Socket;

public class NetworkClient {

    private static final int BUFFER_SIZE = 8192;
    private final String host;
    private final int port;

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private File serverHomeDir;
    private File clientHomeDir;

    public NetworkClient(String serverName, int serverPort) {
        this.host = serverName;
        this.port = serverPort;
    }

    public void connect() throws IOException {
        this.socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        getCommand();
        runReadThread();
        printIntro();
    }

    private void runReadThread() {
        new Thread(() -> {
            while(true) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                try {
                    String[] commandPart = reader.readLine().split(" ", 2);
                    if (commandPart.length > 0 & commandPart[0].equalsIgnoreCase("/list")) {
                        sendCommand(Command.listGetCommand(serverHomeDir));
                        getCommand();
                    } else if (commandPart.length > 1 & commandPart[0].equalsIgnoreCase("/get")) {
                        String fileName = commandPart[1];
                        sendCommand(Command.getFileCommand(new File(serverHomeDir +"\\"+fileName)));
                        getFileFromServer(fileName);
                    } else if (commandPart.length > 1 & commandPart[0].equalsIgnoreCase("/put")) {
                        String fileName = commandPart[1];
                        File inFile = new File(clientHomeDir +"\\"+fileName);
                        if (inFile.exists()) {
                            sendCommand(Command.putFileCommand(inFile));
                            putFileToServer(inFile);
                        } else {
                            System.out.println("Файл не найден!");
                        }
                    } else if (commandPart[0].equalsIgnoreCase("/help")){
                        printIntro();
                    } else if (commandPart[0].equalsIgnoreCase("/exit")){
                        System.exit(0);
                    } else {
                        System.out.println("Неизвестная команда");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void putFileToServer(File file) throws IOException {
        long sizeLocal = file.length();
        out.writeLong(sizeLocal);
        out.flush();
        FileInputStream fileStream = new FileInputStream(file);
        byte[] bytes = new byte[8192];
        int bytesRead;
        int count = (int) (sizeLocal / BUFFER_SIZE) / 10;
        int readBuckets = 0;
        System.out.print("|");
        while ((bytesRead = fileStream.read(bytes)) > 0) {
            readBuckets++;
            if (count > 0) {
                if (readBuckets % count == 0) {
                    System.out.print("=");
                }
            }
            out.write(bytes, 0, bytesRead);
            out.flush();
        }
        System.out.println("|");
        fileStream.close();
    }

    private void getFileFromServer(String fileName) throws IOException {
        if (in.readBoolean()) {
            System.out.println(clientHomeDir.mkdir() ? "Создана папка" + clientHomeDir : "");
            File localFile = new File(clientHomeDir + "/" + fileName);
            FileOutputStream outFile = new FileOutputStream(localFile);

            byte[] bytes = new byte[BUFFER_SIZE];
            long sizeRemote = in.readLong();
            long sizeLocal;
            int bytesRead;
            int count = (int) (sizeRemote / BUFFER_SIZE) / 10;
            int readBuckets = 0;

            System.out.print("|");
            while (true) {
                sizeLocal = localFile.length();
                if (sizeLocal == sizeRemote) break;
                bytesRead = in.read(bytes);
                readBuckets++;
                if (count > 0) {
                    if (readBuckets % count == 0) {
                        System.out.print("=");
                    }
                }
                outFile.write(bytes, 0, bytesRead);
            }
            System.out.println("|");
            outFile.close();
        } else {
            System.out.println("Файл не найден на сервере!");
        }
    }

    private void printIntro() {
        System.out.println("Команды клиента:\n" +
                "/list - список файлов в домашней директории на сервере\n" +
                "/get <filename> - скачать файл\n" +
                "/put <filename> - загрузить файл на сервер\n" +
                "/help - вывести эту справку\n" +
                "/exit - выход из программы");
    }

    private void sendCommand(Command command) throws IOException {
        out.writeObject(command);
    }

    private void getCommand() throws IOException {
        try {
            Command command = (Command) in.readObject();
            switch (command.getType()) {
                case HOME: {
                    HomeCommand commandData = (HomeCommand) command.getData();
                    serverHomeDir =  new File(commandData.getHomeDir());
                    String clientName = "client" + commandData.getClientNum();
                    clientHomeDir = new File("CloudClient/data/" + clientName);
                    break;
                }
                case PUT_LIST: {
                    ListCommand commandData = (ListCommand) command.getData();
                    File[] fileList = commandData.getFileList();
                    for (File file : fileList) {
                        System.out.println(file.getName());
                    }
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Получен неизвестный объект");
        }
    }
}
