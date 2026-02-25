package config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads key/value pairs from {@code config.properties} on the classpath.
 *
 * Key improvements vs original:
 *  - Original used a hard-coded relative file path ("src/test/resources/config.properties")
 *    which breaks when tests are run from a different working directory (e.g. CI server,
 *    IDE run configs).  Loading via the classpath works everywhere Maven puts resources.
 *  - get() now throws a clear IllegalStateException when a key is missing instead of
 *    silently returning null (which produces a NullPointerException deep in test code).
 *  - Static initialiser wraps the IOException properly.
 */
public class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);
    private static final String CONFIG_FILE = "config.properties";

    private static final Properties prop = new Properties();

    static {
        // Classpath-relative load – works regardless of working directory
        try (InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                throw new ExceptionInInitializerError(
                    CONFIG_FILE + " not found on the classpath. " +
                    "Ensure it is in src/test/resources/.");
            }
            prop.load(is);
            log.info("Loaded {} from classpath ({} keys)", CONFIG_FILE, prop.size());
        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                "Failed to load " + CONFIG_FILE + ": " + e.getMessage());
        }
    }

    private ConfigReader() {}

    /**
     * Returns the value for {@code key}.
     *
     * @throws IllegalStateException if the key is absent, preventing silent nulls
     */
    public static String get(String key) {
        String value = prop.getProperty(key);
        if (value == null) {
            throw new IllegalStateException(
                "Missing config key '" + key + "' in " + CONFIG_FILE);
        }
        return value.trim();
    }

    /**
     * Returns the value for {@code key}, or {@code defaultValue} when absent.
     * Use this only when a missing key is genuinely acceptable.
     */
    public static String getOrDefault(String key, String defaultValue) {
        return prop.getProperty(key, defaultValue).trim();
    }
}