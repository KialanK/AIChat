package de.aichat;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIChat implements ClientModInitializer {
	public static final String MOD_ID = "aichat";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private AIChatConfig config;
	private OpenAIAPI openAIAPI;
	private AIChatCommands commands;

	@Override
	public void onInitializeClient() {
		config = new AIChatConfig();
		openAIAPI = new OpenAIAPI(config);
		commands = new AIChatCommands(config, openAIAPI);

		LOGGER.info("Moin Moin");

		commands.registerCommands();
	}
}