package ru.cloudstorage.clientserver;

import java.io.Serializable;

public class AuthorisationCommand implements Serializable {
    private final String login;
    private final String password;
    private boolean isAuthorise;
    private String rootDir;
    private String clientDir;
    private boolean isLogin;
    private String message;

    public AuthorisationCommand(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAuthorise() {
        return isAuthorise;
    }

    public void setAuthorise(boolean authorise) {
        isAuthorise = authorise;
    }

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public String getClientDir() {
        return clientDir;
    }

    public void setClientDir(String clientDir) {
        this.clientDir = clientDir;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setIsLogin(boolean login) {
        isLogin = login;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
