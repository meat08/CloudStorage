package ru.netcloud.clientserver;

import ru.netcloud.clientserver.command.PutGetCommand;
import ru.netcloud.clientserver.command.HomeCommand;
import ru.netcloud.clientserver.command.ListCommand;

import java.io.File;
import java.io.Serializable;

public class Command implements Serializable {

    private CommandType type;
    private Object data;

    public CommandType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public static Command listGetCommand(File dir) {
        Command command = new Command();
        command.type = CommandType.GET_LIST;
        command.data = new ListCommand(dir);
        return command;
    }

    public static Command listPutCommand(File[] fileList) {
        Command command = new Command();
        command.type = CommandType.PUT_LIST;
        command.data = new ListCommand(fileList);
        return command;
    }

    public static Command setHomeDirCommand(String dir, int num) {
        Command command = new Command();
        command.type = CommandType.HOME;
        command.data = new HomeCommand(dir, num);
        return command;
    }

    public static Command getFileCommand(File file) {
        Command command = new Command();
        command.type = CommandType.GET_FILE;
        command.data = new PutGetCommand(file);
        return command;
    }

    public static Command putFileCommand(File file) {
        Command command = new Command();
        command.type = CommandType.PUT_FILE;
        command.data = new PutGetCommand(file);
        return command;
    }
}
