package com.cardshifter.server.model;

import com.cardshifter.core.game.ModCollection;
import com.cardshifter.server.main.ServerConfiguration;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by marc on 2017-02-07.
 */
public class MainServerTest {

    @Test(expected = NoModsLoadedException.class)
    public void testThrowWhenNoModAreLoaded() throws IOException {
        PropertyConfigurator.configure(getClass().getResourceAsStream("../main/log4j.properties"));

        ServerConfiguration config = ServerConfiguration.defaults();
        Path tempFolder = Files.createTempDirectory("temp");
        config.setModsDirectory(tempFolder.toString());
        config.setPortSocket(0);
        config.setPortWebsocket(0);

        MainServer main = new MainServer(config, new Server(), new ModCollection());

        main.start();
    }

}
