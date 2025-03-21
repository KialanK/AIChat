package de.aichat;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.util.Objects;

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

            dispatcher.register(ClientCommandManager.literal("askimage")
                    .then(ClientCommandManager.argument("args", StringArgumentType.greedyString())
                            .executes(this::askImage))
            );

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
                source.sendFeedback(Text.literal("Failed to fetch response: " + e.getMessage()).formatted(Formatting.RED));
                AIChat.LOGGER.error("Error while communicating with OpenAI API", e);
            }
        }).start();

        return 1;
    }

    private int askImage(CommandContext<FabricClientCommandSource> context) {
        String args = StringArgumentType.getString(context, "args");
        FabricClientCommandSource source = context.getSource();

        String[] parts = parsePathAndQuestion(args);
        if(parts == null) {
            source.sendFeedback(Text.literal("Usage: /askimage <path> <question>"));
            return 0;
        }

        String AIModel = config.getModel();
            if (Objects.equals(AIModel, "gpt-3.5-turbo")) {
                source.sendFeedback(Text.literal("Error: " + AIModel + " is a model wich cannot analyze images please use any gpt-4 model or just press the reset button in the config screen").formatted(Formatting.RED));
                source.sendFeedback(Text.literal("[Change model]").formatted(Formatting.BOLD).styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/config AI_MODEL gpt-4o-mini"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Automatically change to model gpt-4o-mini")))
                ));
                return 1;
            }

        String imagePath = normalizePath(parts[0]);
        String question = parts[1];

        source.sendFeedback(Text.literal("Analyzing image..."));

        new Thread(() -> {
            try {
                OpenAIImageAPI imageAPI = new OpenAIImageAPI(config);
                String response = imageAPI.analyzeImage(imagePath, question);
                source.sendFeedback(Text.literal("Response: " + response));
            } catch (Exception e) {
                source.sendFeedback(Text.literal("Failed to fetch response: " + e.getMessage()).formatted(Formatting.RED));
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

    private String[] parsePathAndQuestion(String args) {
        args = args.trim();
        if(args.isEmpty()) return null;

        if(args.startsWith("\"")) {
            int endQuote = args.indexOf("\"", 1);
            if(endQuote == -1) return null;

            String path = args.substring(1, endQuote);
            String question = args.substring(endQuote + 1).trim();
            if(question.isEmpty()) return null;

            return new String[]{path, question};
        }

        int firstSpace = args.indexOf(" ");
        if(firstSpace == -1) return null;

        return new String[]{
                args.substring(0, firstSpace),
                args.substring(firstSpace + 1)
        };
    }

    private String normalizePath(String path) {
        return path.replace("/", File.separator)
                .replace("\\", File.separator)
                .replace("\"", "");
    }
}