package de.aichat;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class AIChatConfigButton {
    private static boolean openScreen = false;
    private static AIChatConfig config;

    public static void setConfig(AIChatConfig config) {
        AIChatConfigButton.config = config;
    }

    public static void afterScreenInit(MinecraftClient client, Screen screen, int sw, int sh) {
        if (screen instanceof GameMenuScreen) {
            List<ClickableWidget> buttons = Screens.getButtons(screen);

            int guiScale = MinecraftClient.getInstance().options.getGuiScale().getValue();
            int buttonHeight = 8;
            if (guiScale > 3) {buttonHeight = 9;}
            int x = sw / 2 + 106;
            int y = sh / 4 + buttonHeight;
            buttons.add(ButtonWidget.builder(Text.literal("\uD83D\uDEE0").formatted(Formatting.BOLD), button -> {
                openScreen = true;
            }).dimensions(x, y, 20, 20).tooltip(Tooltip.of(Text.literal("Open AIChat Configuration"))).build());
        }
    }

    public static void tick() {
        if (openScreen) {
            openScreen = false;
            MinecraftClient.getInstance().setScreen(new AIChatConfigScreen(null, config));
        }
    }
}
