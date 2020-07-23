package ru.netcloud.clientserver.command;

import java.io.Serializable;

public class HomeCommand implements Serializable {
    private final String homeDir;
    private final int clientNum;

    public HomeCommand(String dir, int num) {
        this.homeDir = dir;
        this.clientNum = num;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public int getClientNum() {
        return clientNum;
    }
}
