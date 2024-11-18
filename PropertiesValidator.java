package com.example.validation;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class PropertiesValidator {
    private static final Pattern VALID_KEY_VALUE_PATTERN = Pattern.compile("^[^\\s=]+=[^\\s]+$");

    public static void main(String[] args) {
        try {
            // Step 1: Find all properties files in the entire classpath
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("");

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                scanClasspath(resource);
            }
        } catch (Exception e) {
            System.err.println("Error during properties validation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Method to scan the classpath for properties files
    private static void scanClasspath(URL resource) throws IOException {
        URLConnection connection = resource.openConnection();

        if (connection instanceof JarURLConnection) {
            // Handle JAR files
            JarFile jarFile = ((JarURLConnection) connection).getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".properties")) {
                    System.out.println("Found properties file in JAR: " + entry.getName());
                    validatePropertiesFromJar(jarFile, entry);
                }
            }
        } else {
            // Handle regular file system
            String path = resource.getPath();
            if (path.endsWith(".properties")) {
                System.out.println("Found properties file: " + path);
                validatePropertiesFromClasspath(path);
            }
        }
    }

    // Method to load and validate properties from a JAR entry
    private static void validatePropertiesFromJar(JarFile jarFile, JarEntry entry) {
        try (InputStream inputStream = jarFile.getInputStream(entry)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            validateProperties(properties);
        } catch (IOException e) {
            System.err.println("Error reading properties from JAR: " + entry.getName());
        }
    }

    // Method to load and validate properties from the classpath
    private static void validatePropertiesFromClasspath(String filePath) {
        try (InputStream inputStream = PropertiesValidator.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                System.err.println("Properties file not found: " + filePath);
                return;
            }
            Properties properties = new Properties();
            properties.load(inputStream);
            validateProperties(properties);
        } catch (IOException e) {
            System.err.println("Error reading properties file: " + filePath);
        }
    }

    // Method to validate the properties content
    private static void validateProperties(Properties properties) {
        Set<String> keys = properties.stringPropertyNames();
        boolean isValid = true;

        for (String key : keys) {
            String value = properties.getProperty(key);

            // Check if the key or value is missing or contains spaces
            if (key.trim().isEmpty() || value == null || value.trim().isEmpty()) {
                System.err.println("Invalid property: key='" + key + "', value='" + value + "'");
                isValid = false;
                continue;
            }

            // Check if the key-value pair follows the pattern
            if (!VALID_KEY_VALUE_PATTERN.matcher(key + "=" + value).matches()) {
                System.err.println("Malformed key-value pair: " + key + "=" + value);
                isValid = false;
            }
        }

        if (!isValid) {
            System.err.println("Properties file validation failed.");
        } else {
            System.out.println("Properties file is valid.");
        }
    }
}

/*


<build>
    <plugins>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>exec-maven-plugin</artifactId>
            <version>3.0.0</version>
            <executions>
                <execution>
                    <!-- Change the phase to `compile` or `post-compile` -->
                    <phase>compile</phase>
                    <goals>
                        <goal>java</goal>
                    </goals>
                    <configuration>
                        <mainClass>com.example.validation.PropertiesValidator</mainClass>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>


<build>
    <plugins>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>exec-maven-plugin</artifactId>
            <version>3.1.0</version>
            <executions>
                <execution>
                    <phase>verify</phase>
                    <goals>
                        <goal>java</goal>
                    </goals>
                    <configuration>
                        <mainClass>com.example.validation.PropertiesValidator</mainClass>
                        <arguments></arguments>
                        <classpathScope>runtime</classpathScope>
                        <includeProjectDependencies>true</includeProjectDependencies>
                        <includePluginDependencies>true</includePluginDependencies>
                        <classpath>
                            <path>${project.build.outputDirectory}</path>
                        </classpath>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>



*/
