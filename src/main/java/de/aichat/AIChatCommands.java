package de.aichat;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class AIChatCommands {
    private final AIChatConfig config;
    private final OpenAIAPI openAIAPI;
    private static boolean openScreen = false;

    public AIChatCommands(AIChatConfig config, OpenAIAPI openAIAPI) {
        this.config = config;
        this.openAIAPI = openAIAPI;
    }

    public void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // Command to interact with OpenAI
            dispatcher.register(ClientCommandManager.literal("ask")
                    .then(ClientCommandManager.argument("question", StringArgumentType.greedyString())
                            .executes(this::askOpenAI)));

            // Command to configure settings
            dispatcher.register(ClientCommandManager.literal("config")
                    .executes(context -> {
                        openScreen = true;
                        return 1;
                    })
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

    public void tick() {
        if (openScreen) {
            openScreen = false;
            MinecraftClient.getInstance().setScreen(new AIChatConfigScreen(null, config));
        }
    }

    private int askOpenAI(CommandContext<FabricClientCommandSource> context) {
        String question = StringArgumentType.getString(context, "question");
        FabricClientCommandSource source = context.getSource();

        source.sendFeedback(Text.literal("Generating response..."));

        new Thread(() -> {
            try {
                String response = openAIAPI.chatGPT(question);
                source.sendFeedback(Text.literal("Response: " + response));
            } catch (Exception e) {
                source.sendFeedback(Text.literal("Failed to fetch response: " + e.getMessage()));
                AIChat.LOGGER.error("Error while communicating with OpenAI API", e);
            }
        }).start();

        return 1;
    }

    private int setConfig(CommandContext<FabricClientCommandSource> context, String key) {
        String value = StringArgumentType.getString(context, "value");
        FabricClientCommandSource source = context.getSource();

        switch (key) {
            case "OPENAI_API_KEY":
                config.setApiKey(value);
                break;
            case "ROLE_OF_THE_AI":
                config.setContent(value);
                break;
            case "AI_MODEL":
                config.setModel(value);
                break;
            default:
                source.sendFeedback(Text.literal("Invalid config key: " + key));
                return 0;
        }

        config.saveConfig();
        source.sendFeedback(Text.literal("Configuration updated: " + key + " = " + value));
        return 1;
    }
}