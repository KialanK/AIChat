package de.aichat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AIChatConfig {
    private String apiKey;
    private String content;
    private String model;

    private static final String CONFIG_FILE_PATH = "config/aichat-config.json";

    public AIChatConfig() {
        loadConfig();
    }

    public void loadConfig() {
        File configFile = new File(CONFIG_FILE_PATH);

        if (!configFile.exists()) {
            AIChat.LOGGER.warn("Config file not found, creating default config.");
            createDefaultConfig();
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();
            apiKey = config.get("OPENAI_API_KEY").getAsString();
            content = config.get("ROLE_OF_THE_AI").getAsString();
            model = config.get("AI_MODEL").getAsString();
        } catch (IOException e) {
            AIChat.LOGGER.error("Failed to load config file", e);
        }
    }

    public void saveConfig() {
        JsonObject config = new JsonObject();
        config.add("OPENAI_API_KEY", new JsonPrimitive(apiKey));
        config.add("ROLE_OF_THE_AI", new JsonPrimitive(content));
        config.add("AI_MODEL", new JsonPrimitive(model));

        File configFile = new File(CONFIG_FILE_PATH);
        configFile.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(config.toString());
            writer.flush();
            AIChat.LOGGER.info("Config file saved successfully.");
        } catch (IOException e) {
            AIChat.LOGGER.error("Failed to save config file", e);
        }
    }

    private void createDefaultConfig() {
        apiKey = "";
        content = "You are an assistant implemented in a Minecraft Mod to help players and you give short answers with a maximum length of 4 short Sentences";
        model = "gpt-4o-mini";
        saveConfig();
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}