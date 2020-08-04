package ru.cloudstorage.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.cloudstorage.clientserver.FileList;
import ru.cloudstorage.clientserver.GetFileListCommand;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Клиент подключился");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof GetFileListCommand) {
            Path path = Paths.get(((GetFileListCommand) msg).getPath());
            FileList fileList = new FileList();
            fileList.formFileInfoList(path);
            ctx.writeAndFlush(fileList);
        } else {
            System.out.println("Неизвестный класс");
        }
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
