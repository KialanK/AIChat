package de.aichat.mixin;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static net.minecraft.client.util.ScreenshotRecorder.takeScreenshot;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotRecorderMixin {
    private static File getScreenshotFilename(File directory) {
        String string = Util.getFormattedCurrentTime();
        int i = 1;

        while(true) {
            File file = new File(directory, string + (i == 1 ? "" : "_" + i) + ".png");
            if (!file.exists()) {
                return file;
            }

            ++i;
        }
    }

    @Inject(
            at = {@At("HEAD")},
            method = {"saveScreenshotInner"}
    )
    private static void modifyScreenshotMessage(File gameDirectory, String fileName, Framebuffer framebuffer, Consumer<Text> messageReceiver, CallbackInfo ci) {
        NativeImage nativeImage = takeScreenshot(framebuffer);
        File file = new File(gameDirectory, "screenshots");
        file.mkdir();
        File file2;
        if (fileName == null) {
            file2 = getScreenshotFilename(file);
        } else {
            file2 = new File(file, fileName);
        }

        Util.getIoWorkerExecutor().execute(() -> {
            try {
                nativeImage.writeTo(file2);
                Thread.sleep(10);
                Text upload = Text.literal("idk");
                    messageReceiver.accept(Text.translatable("[Send to OpenAI]", upload).formatted(Formatting.BOLD).styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/askimage screenshots/" + file2.getName()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Ask ChatGPT something about this picture!")))
                    ));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                nativeImage.close();
            }
        });
    }
}