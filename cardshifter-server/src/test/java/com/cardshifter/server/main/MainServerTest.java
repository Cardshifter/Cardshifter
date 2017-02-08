package com.cardshifter.server.main;

import com.cardshifter.server.model.MainServer;
import com.cardshifter.server.model.Server;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

/**
 * Created by marc on 2017-02-07.
 */
public class MainServerTest {

    @Test(expected = IllegalStateException.class)
    public void testThrowWhenNoModAreLoaded() {
        PropertyConfigurator.configure(getClass().getResourceAsStream("log4j.properties"));

        ServerConfiguration config = ServerConfiguration.defaults();
        config.setModsDirectory(System.getProperty("user.home"));
        config.setPortSocket(0);
        config.setPortWebsocket(0);

        MainServer main = new MainServer(config, new Server());

        main.start();
    }

}
