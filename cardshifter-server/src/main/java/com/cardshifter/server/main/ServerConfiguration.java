package com.cardshifter.server.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Simon on 5/29/2015.
 */
public class ServerConfiguration {

    private int portSocket = 4242;
    private int portWebsocket = 4243;

    private String modsDirectory = "";

    public static ServerConfiguration readFrom(String s) {
        Properties properties = new Properties();
        try (FileInputStream is = new FileInputStream(new File(s))) {
            properties.load(is);
        } catch (IOException ex) {
            System.out.println("Cannot read properties: " + ex + ", using defaults.");
            // use defaults
        }
        ServerConfiguration config = new ServerConfiguration();
        config.portSocket = Integer.parseInt(properties.getProperty("port", "4242"));
        config.portWebsocket = Integer.parseInt(properties.getProperty("websocket-port", "4243"));
        config.modsDirectory = properties.getProperty("mods", "");
        return config;
    }

    public int getPortSocket() {
        return portSocket;
    }

    public int getPortWebsocket() {
        return portWebsocket;
    }

    public String getModsDirectory() {
        return modsDirectory;
    }

    public void setModsDirectory(String modsDirectory) {
        this.modsDirectory = modsDirectory;
    }

    public void setPortSocket(int portSocket) {
        this.portSocket = portSocket;
    }

    public void setPortWebsocket(int portWebsocket) {
        this.portWebsocket = portWebsocket;
    }

    public static ServerConfiguration defaults() {
        return new ServerConfiguration();
    }
}
