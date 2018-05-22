package com.cardshifter.server.main;

import org.apache.log4j.LogManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Simon on 5/29/2015.
 */
public class ServerConfiguration {

    public enum WarningLevel {
        FAIL, WARN, IGNORE
    }

    private int portSocket = 4242;
    private int portWebsocket = 4243;

    private String modsDirectory = "extra-mods";
    private WarningLevel missingSecurity = WarningLevel.WARN;

    public static ServerConfiguration readFrom(String s) {
        Properties properties = new Properties();
        try (FileInputStream is = new FileInputStream(new File(s))) {
            properties.load(is);
        } catch (IOException ex) {
            LogManager.getLogger(ServerConfiguration.class).warn("Cannot read properties: " + ex + ", using defaults.");
            // use defaults
        }
        ServerConfiguration config = new ServerConfiguration();
        config.portSocket = Integer.parseInt(properties.getProperty("port", "4242"));
        config.portWebsocket = Integer.parseInt(properties.getProperty("websocket-port", "4243"));
        config.modsDirectory = properties.getProperty("mods", "extra-mods");
        config.missingSecurity = WarningLevel.valueOf(properties.getProperty("missing-security", WarningLevel.WARN.name()));
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

    public WarningLevel getMissingSecurity() {
        return missingSecurity;
    }

    public void setMissingSecurity(WarningLevel missingSecurity) {
        this.missingSecurity = missingSecurity;
    }

    public static ServerConfiguration defaults() {
        return new ServerConfiguration();
    }
}
