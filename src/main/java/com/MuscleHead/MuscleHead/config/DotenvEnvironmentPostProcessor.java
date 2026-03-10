package com.MuscleHead.MuscleHead.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Loads .env file into Spring Environment with highest priority, so values are
 * available before datasource and other beans are configured.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String DOTENV_PROPERTY_SOURCE = "dotenv";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envPath = findEnvFile();
        if (envPath == null || !Files.exists(envPath)) {
            return;
        }
        Map<String, Object> envVars = parseEnvFile(envPath);
        if (!envVars.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(DOTENV_PROPERTY_SOURCE, envVars));
        }
    }

    private Path findEnvFile() {
        // Check working directory first
        Path cwd = Paths.get("").toAbsolutePath();
        Path envInCwd = cwd.resolve(".env");
        if (Files.exists(envInCwd)) {
            return envInCwd;
        }
        // Fallback: look in user.dir (often project root when running from IDE)
        String userDir = System.getProperty("user.dir");
        if (userDir != null) {
            Path envInUserDir = Paths.get(userDir).resolve(".env");
            if (Files.exists(envInUserDir)) {
                return envInUserDir;
            }
        }
        return null;
    }

    private Map<String, Object> parseEnvFile(Path path) {
        Map<String, Object> result = new HashMap<>();
        try (Stream<String> lines = Files.lines(path)) {
            lines.filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
                    .forEach(line -> {
                        int eq = line.indexOf('=');
                        if (eq > 0) {
                            String key = line.substring(0, eq).trim();
                            String value = line.substring(eq + 1).trim();
                            // Remove surrounding quotes if present
                            if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"")
                                    || value.startsWith("'") && value.endsWith("'"))) {
                                value = value.substring(1, value.length() - 1);
                            }
                            result.put(key, value);
                        }
                    });
        } catch (IOException ignored) {
            // Silently skip if .env can't be read
        }
        return result;
    }
}
