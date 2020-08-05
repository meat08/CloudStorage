package ru.cloudstorage.server.database;

import org.apache.log4j.Logger;

import java.sql.*;

public class DatabaseService {
    private Connection connection = null;
    private String URL = "jdbc:sqlite:CloudServer/src/main/java/ru/cloudstorage/server/database/CloudServer.sqlite";
    private static final Logger logger = Logger.getLogger(DatabaseService.class);

    public void start() {
        try {
            connection = DriverManager.getConnection(URL);
            logger.info("База данных подключена");
            System.out.println("База данных подключена");
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к базе данных");
            logger.fatal("Ошибка подключения к базе данных");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void stop() {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isAuthorise(String login, String password) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT id FROM users WHERE login = ? AND password = ?"
            );
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
