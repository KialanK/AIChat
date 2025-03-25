package de.aichat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AIChat implements ClientModInitializer {
	public static final String MOD_ID = "aichat";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private AIChatConfig config;
	private OpenAIAPI openAIAPI;
	private AIChatCommands commands;
	private OpenAIImageAPI imageAPI;

	@Override
	public void onInitializeClient() {
		imageAPI = new OpenAIImageAPI(config);
		config = new AIChatConfig();
		openAIAPI = new OpenAIAPI(config);
		commands = new AIChatCommands(config, openAIAPI);
		AIChatConfigButton.setConfig(config);

		ClientTickEvents.START_CLIENT_TICK.register(client -> commands.tick());
		ClientTickEvents.START_CLIENT_TICK.register(client -> AIChatConfigButton.tick());

		ScreenEvents.AFTER_INIT.register(AIChatConfigButton::afterScreenInit);

		LOGGER.info("Moin Moin");

		commands.registerCommands();
	}
}