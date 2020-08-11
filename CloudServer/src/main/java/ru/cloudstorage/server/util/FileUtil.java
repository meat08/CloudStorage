package ru.cloudstorage.server.util;

import org.apache.log4j.Logger;
import ru.cloudstorage.server.NetworkServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    private static final Path DEFAULT_ROOT_DIR = Paths.get(".", "data");
    private static final Logger logger = Logger.getLogger(NetworkServer.class);

    public static String[] createHomeDir(String login) throws IOException {
        Path clientPath;
        String[] paths = new String[2];
        try {
            String rootDir = new ServerProperties().getRootDir();
            clientPath = Paths.get(rootDir, login);
            paths[0] = rootDir;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Файл конфигурации не обнаружен. Использован каталог по умолчанию.");
            logger.info("Файл конфигурации не обнаружен. Использован каталог по умолчанию.");
            clientPath = Paths.get(DEFAULT_ROOT_DIR.toString(), login);
            paths[0] = DEFAULT_ROOT_DIR.toString();
        }
        if (!Files.exists(clientPath)) {
            Files.createDirectories(clientPath);
        }
        paths[1] = clientPath.toString();
        return paths;
    }
}
