package de.aichat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AIChat implements ClientModInitializer {
	public static final String MOD_ID = "aichat";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Configuration variables
	private String apiKey;
	private String content;
	private String model;

	// Path to the configuration file
	private static final String CONFIG_FILE_PATH = "config/aichat-config.json";

	@Override
	public void onInitializeClient() {
		// Load the configuration from file
		loadConfig();

		// Log initialization message
		LOGGER.info("Moin Moin");

		// Register the commands
// Register the commands
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			// Command to interact with OpenAI
			dispatcher.register(ClientCommandManager.literal("ask")
					.then(ClientCommandManager.argument("question", StringArgumentType.greedyString())
							.executes(this::askOpenAI)));

			// Command to configure settings
			dispatcher.register(ClientCommandManager.literal("config")
					.then(ClientCommandManager.literal("OPENAI_API_KEY")
							.then(ClientCommandManager.argument("value", StringArgumentType.string())
									.executes(context -> setConfig(context, "OPENAI_API_KEY"))))
					.then(ClientCommandManager.literal("ROLE_OF_THE_AI")
							.then(ClientCommandManager.argument("value", StringArgumentType.greedyString())
									.executes(context -> setConfig(context, "ROLE_OF_THE_AI"))))
					.then(ClientCommandManager.literal("AI_MODEL")
							.then(ClientCommandManager.argument("value", StringArgumentType.string())
									.executes(context -> setConfig(context, "AI_MODEL")))));
		});
	}

	// Load configuration from a file
	private void loadConfig() {
		File configFile = new File(CONFIG_FILE_PATH);

		// Check if the configuration file exists
		if (!configFile.exists()) {
			LOGGER.warn("Config file not found, creating default config.");
			createDefaultConfig();
			return;
		}

		// Load configuration from file
		try (FileReader reader = new FileReader(configFile)) {
			JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();
			apiKey = config.get("OPENAI_API_KEY").getAsString();
			content = config.get("ROLE_OF_THE_AI").getAsString();
			model = config.get("AI_MODEL").getAsString();
		} catch (IOException e) {
			LOGGER.error("Failed to load config file", e);
		}
	}

	// Save the current configuration to a file
	private void saveConfig() {
		JsonObject config = new JsonObject();
		config.add("OPENAI_API_KEY", new JsonPrimitive(apiKey));
		config.add("ROLE_OF_THE_AI", new JsonPrimitive(content));
		config.add("AI_MODEL", new JsonPrimitive(model));

		File configFile = new File(CONFIG_FILE_PATH);

		// Ensure the directory exists
		configFile.getParentFile().mkdirs(); // Ensure the directory exists

		// Write configuration to file
		try (FileWriter writer = new FileWriter(configFile)) {
			writer.write(config.toString());
			writer.flush();
			LOGGER.info("Config file saved successfully.");
		} catch (IOException e) {
			LOGGER.error("Failed to save config file", e);
		}
	}

	// Create a default configuration file if none exists
	private void createDefaultConfig() {
		apiKey = "use /config OPENAI_API_KEY to insert you API key";
		content = "You are an assistant implemented in a Minecraft Mod to help players and you give short answers with a maximum length of 4 short Sentences";
		model = "gpt-3.5-turbo";
		saveConfig();
	}

	// Handle the /ask command
	private int askOpenAI(CommandContext<FabricClientCommandSource> context) {
		String question = StringArgumentType.getString(context, "question");
		FabricClientCommandSource source = context.getSource();

		// Notify the user that the response is being generated
		source.sendFeedback(Text.literal("Generating response..."));

		// Run the OpenAI API call in a separate thread
		new Thread(() -> {
			try {
				String response = chatGPT(question);
				source.sendFeedback(Text.literal("Response: " + response));
			} catch (Exception e) {
				source.sendFeedback(Text.literal("Failed to fetch response: " + e.getMessage()));
				LOGGER.error("Error while communicating with OpenAI API", e);
			}
		}).start();

		return 1; // Command executed successfully
	}

	// Handle the /config command
	private int setConfig(CommandContext<FabricClientCommandSource> context, String key) {
		String value = StringArgumentType.getString(context, "value");
		FabricClientCommandSource source = context.getSource();

		// Update the configuration based on the key
		switch (key) {
			case "OPENAI_API_KEY":
				apiKey = value;
				break;
			case "ROLE_OF_THE_AI":
				content = value;
				break;
			case "AI_MODEL":
				model = value;
				break;
			default:
				source.sendFeedback(Text.literal("Invalid config key: " + key));
				return 0; // Command failed
		}

		saveConfig();
		source.sendFeedback(Text.literal("Configuration updated: " + key + " = " + value));
		return 1; // Command executed successfully
	}

	// Communicate with the OpenAI API
	public String chatGPT(String message) {
		String url = "https://api.openai.com/v1/chat/completions";

		try {
			// Create the HTTP POST request
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Authorization", "Bearer " + apiKey);
			con.setRequestProperty("Content-Type", "application/json");

			// Build the request body
			String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + content + message + "\"}]}";
			con.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
			writer.write(body);
			writer.flush();
			writer.close();

			// Get the response
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// Return the extracted contents of the response
			return extractContentFromResponse(response.toString());

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// Extract the "content" field from the API response
	public static String extractContentFromResponse(String response) {
		int startMarker = response.indexOf("content") + 11; // Marker for where the content starts.
		int endMarker = response.indexOf("\"", startMarker); // Marker for where the content ends.
		String rawContent = response.substring(startMarker, endMarker); // Extract the raw content

		// Excludes newlines and returns the response
		return rawContent.replace("\\n", " ");
	}
}