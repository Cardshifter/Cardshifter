
package com.cardshifter.core.modloader;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 *
 * @author Frank van Heeswijk
 */
final class ModLoaderHelper {
    private ModLoaderHelper() {
        throw new UnsupportedOperationException();
    }
    
    static String getConfigurationFileName() {
        return "configuration.properties";
    }
    
    static Properties getConfiguration(final Path modDirectory) throws IOException {
        Objects.requireNonNull(modDirectory, "modDirectory");
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(modDirectory.resolve(getConfigurationFileName()).toFile())) {
            properties.load(fileInputStream);
        }
        return properties;
    }
}
