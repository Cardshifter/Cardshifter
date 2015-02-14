package com.cardshifter.modapi.base;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for mods
 */
public class ModHelper {

    /**
     * Get the Path for a filename, either using internal resources or using an external file
     * @param mod The Mod that requests the file
     * @param file The filename requested
     * @return Path object for the requested file
     */
    public static Path getPath(ECSMod mod, String file) {
        URL url = mod.getClass().getResource(file);
        if (url != null) {
            try {
                Path resource = Paths.get(url.toURI());
                if (Files.exists(resource)) {
                    return resource;
                }
            } catch (URISyntaxException e) { }
        }

        Path path = Paths.get(file);
        return path;
    }

}
