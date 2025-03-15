package de.aichat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class AIChatConfigScreen extends Screen {
    private final Screen parent;
    private final AIChatConfig config;

    private static TextFieldWidget apiKeyField;
    private static TextFieldWidget roleField;
    private static TextFieldWidget modelField;

    public AIChatConfigScreen(Screen parent, AIChatConfig config) {
        super(Text.literal("AIChat Configuration"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        // API Key Field
        apiKeyField = new TextFieldWidget(
                textRenderer,
                width / 2 - 100,
                60,
                200,
                20,
                Text.literal("Insert your API Key")
        );
        apiKeyField.setMaxLength(256);
        apiKeyField.setText(config.getApiKey());
        addDrawableChild(apiKeyField);

        // Role Field
        roleField = new TextFieldWidget(
                textRenderer,
                width / 2 - 100,
                100,
                200,
                20,
                Text.literal("Role of the AI")
        );
        roleField.setMaxLength(256);
        roleField.setText(config.getContent());
        addDrawableChild(roleField);

        // Model Field
        modelField = new TextFieldWidget(
                textRenderer,
                width / 2 - 100,
                140,
                200,
                20,
                Text.literal("AI Model")
        );
        modelField.setMaxLength(256);
        modelField.setText(config.getModel());
        addDrawableChild(modelField);

        //Clear Button for the key field
        addDrawableChild(ButtonWidget.builder(Text.literal("Clear"), button -> {
            apiKeyField.setText("");
        }).dimensions(width / 2 - 210, 60, 100, 20).build());

        //Clear Button for the role field
        addDrawableChild(ButtonWidget.builder(Text.literal("Clear"), button -> {
            roleField.setText("");
        }).dimensions(width / 2 - 210, 100, 100, 20).build());

        //Clear Button for the model field
        addDrawableChild(ButtonWidget.builder(Text.literal("Clear"), button -> {
            modelField.setText("");
        }).dimensions(width / 2 - 210, 140, 100, 20).build());

        //Reset Button for the api field
        addDrawableChild(ButtonWidget.builder(Text.literal("Get API Key"), button -> {
            Util.getOperatingSystem().open("https://platform.openai.com/settings/organization/api-keys");
        }).dimensions(width / 2 + 110, 60, 100, 20).build());

        //Reset Button for the role field
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
            roleField.setText("You are an assistant implemented in a Minecraft Mod to help players and you give short answers with a maximum length of 4 short Sentences");
        }).dimensions(width / 2 + 110, 100, 100, 20).build());

        //Reset Button for the model field
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
            modelField.setText("gpt-3.5-turbo");
        }).dimensions(width / 2 + 110, 140, 100, 20).build());

        // Save Button
        addDrawableChild(ButtonWidget.builder(Text.literal("Save"), button -> {
            config.setApiKey(apiKeyField.getText());
            config.setContent(roleField.getText());
            config.setModel(modelField.getText());
            config.saveConfig();
            close();
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("Configuration updated!"), false);
            }
        }).dimensions(width / 2 - 50, height - 90, 100, 20).build());

        // Cancel Button
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> close())
                .dimensions(width / 2 - 50, height - 60, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Draw Titles
        context.drawTextWithShadow(
                textRenderer,
                Text.literal("API Key"),
                width / 2 - 100,
                50,
                0xFFFFFF
        );
        context.drawTextWithShadow(
                textRenderer,
                Text.literal("Role of the AI"),
                width / 2 - 100,
                90,
                0xFFFFFF
        );
        context.drawTextWithShadow(
                textRenderer,
                Text.literal("AI Model"),
                width / 2 - 100,
                130,
                0xFFFFFF
        );
    }

    @Override
    public void close() {
        client.setScreen(parent);

    }
}