package com.example.validation;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.List;
import java.util.ArrayList;

public class PropertiesValidator {
    public static void main(String[] args) throws IOException {
        List<URL> propertyFiles = findPropertyFiles();
        if (propertyFiles.isEmpty()) {
            System.out.println("No property files found.");
        } else {
            for (URL url : propertyFiles) {
                System.out.println("Found property file: " + url);
            }
        }
    }

    // Method to find all .properties files in the classpath
    public static List<URL> findPropertyFiles() throws IOException {
        List<URL> propertyFiles = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (classLoader instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) classLoader).getURLs();
            for (URL url : urls) {
                if (url.getProtocol().equals("file") && url.getPath().endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(url.getPath())) {
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            if (entry.getName().endsWith(".properties")) {
                                propertyFiles.add(new URL("jar:file:" + url.getPath() + "!/" + entry.getName()));
                            }
                        }
                    }
                }
            }
        }
        return propertyFiles;
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
