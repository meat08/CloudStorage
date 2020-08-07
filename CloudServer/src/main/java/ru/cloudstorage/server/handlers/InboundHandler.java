package ru.cloudstorage.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;
import ru.cloudstorage.clientserver.*;
import ru.cloudstorage.server.NetworkServer;
import ru.cloudstorage.server.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class InboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(NetworkServer.class);
    private String login;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Клиент подключился. Addr: " + ctx.channel().remoteAddress());
        logger.info("Клиент подключился. Addr: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Клиент отключился. Addr: " + ctx.channel().remoteAddress() + " Login: " + login);
        NetworkServer.getDatabaseService().setIsLogin(login, false);
        logger.info("Клиент отключился. Addr: " + ctx.channel().remoteAddress() + " Login: " + login);
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof GetFileListCommand) {
            sendFileList(ctx, (GetFileListCommand) msg);
        } else if (msg instanceof AuthorisationCommand) {
            authorisationProcess(ctx, (AuthorisationCommand) msg);
        } else if (msg instanceof FileMessageCommand) {
            getFileFromClient((FileMessageCommand) msg);
        } else if (msg instanceof FileRequestCommand) {
            sendFileToClient(ctx, (FileRequestCommand) msg);
        } else if (msg instanceof RemoveFileCommand) {
            deleteFile((RemoveFileCommand) msg);
        } else {
            logger.error("Получен неизвестный объект от клиента " + ctx.channel().remoteAddress());
        }
    }

    private void deleteFile(RemoveFileCommand msg) {
        try {
            Files.delete(Paths.get(msg.getFilePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFileToClient(ChannelHandlerContext ctx, FileRequestCommand msg) {
        new Thread(() -> {
            try {
                File file = new File(msg.getFilePath());
                int bufSize = 1024 * 1024 * 10;
                int partsCount = new Long(file.length() / bufSize).intValue();
                if (file.length() % bufSize != 0) {
                    partsCount++;
                }
                FileMessageCommand fileMessage = new FileMessageCommand(file.getName(), -1, partsCount, new byte[bufSize]);
                FileInputStream in = new FileInputStream(file);
                for (int i = 0; i < partsCount; i++) {
                    int readBytes = in.read(fileMessage.data);
                    fileMessage.partNumber = i+1;
                    if (readBytes < bufSize) {
                        fileMessage.data = Arrays.copyOfRange(fileMessage.data, 0, readBytes);
                    }
                    ctx.writeAndFlush(fileMessage);
                    Thread.sleep(100);
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getFileFromClient(FileMessageCommand msg) {
        try {
            boolean append = true;
            if (msg.partNumber == 1) {
                append = false;
            }
            FileOutputStream fos = new FileOutputStream(msg.dstFilePath, append);
            fos.write(msg.data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authorisationProcess(ChannelHandlerContext ctx, AuthorisationCommand msg) throws IOException {
        this.login = msg.getLogin();
        String password = msg.getPassword();
        boolean isAuthorise = NetworkServer.getDatabaseService().isAuthorise(login, password);
        boolean isLogin = NetworkServer.getDatabaseService().isLogin(login);
        msg.setAuthorise(isAuthorise);
        if (isLogin) {
            msg.setIsLogin(true);
            msg.setMessage("Клиент с таким логином уже авторизован.");
        }
        if (isAuthorise & !isLogin) {
            String[] paths = FileUtil.createHomeDir(login);
            NetworkServer.getDatabaseService().setIsLogin(login, true);
            msg.setRootDir(paths[0]);
            msg.setClientDir(paths[1]);
        }
        ctx.writeAndFlush(msg);
    }

    private void sendFileList(ChannelHandlerContext ctx, GetFileListCommand msg) {
        Path path = Paths.get(msg.getPath());
        FileListCommand fileList = new FileListCommand();
        fileList.formFileInfoList(path);
        ctx.writeAndFlush(fileList);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            System.out.println("Клиент разорвал соединение");
            logger.info("Клиент разорвал соединение. Adr: " + ctx.channel().remoteAddress() + " Login: " + login);
            NetworkServer.getDatabaseService().setIsLogin(login, false);
        } else {
            cause.printStackTrace();
            logger.fatal("Возникло исключение: " + cause.getMessage());
        }
        ctx.close();
    }
}
