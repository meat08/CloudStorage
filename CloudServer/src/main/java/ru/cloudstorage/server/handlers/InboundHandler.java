package ru.cloudstorage.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.cloudstorage.clientserver.*;
import ru.cloudstorage.server.NetworkServer;
import ru.cloudstorage.server.util.FileUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Клиент подключился. Addr: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Клиент отключился. Addr: " + ctx.channel().remoteAddress());
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof GetFileListCommand) {
            sendFileList(ctx, (GetFileListCommand) msg);
        } else if (msg instanceof AuthorisationCommand) {
            authorisationProcess(ctx, (AuthorisationCommand) msg);
        } else if (msg instanceof FileMessage) {
            getFileFromClient(ctx, (FileMessage) msg);
        } else {
            System.out.println("Неизвестный класс");
        }
    }

    private void getFileFromClient(ChannelHandlerContext ctx, FileMessage msg) {
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
        String login = msg.getLogin();
        String password = msg.getPassword();
        boolean isAuthorise = NetworkServer.getDatabaseService().isAuthorise(login, password);
        msg.setAuthorise(isAuthorise);
        if (isAuthorise) {
            String[] paths = FileUtil.createHomeDir(login);
            msg.setRootDir(paths[0]);
            msg.setClientDir(paths[1]);
        }
        ctx.writeAndFlush(msg);
    }

    private void sendFileList(ChannelHandlerContext ctx, GetFileListCommand msg) {
        Path path = Paths.get(msg.getPath());
        FileList fileList = new FileList();
        fileList.formFileInfoList(path);
        ctx.writeAndFlush(fileList);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            System.out.println("Клиент разорвал соединение");
        } else {
            cause.printStackTrace();
        }
        ctx.close();
    }
}
