package com.tesselslate.pastry.mixin.cullvis;

import com.tesselslate.pastry.Pastry;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Keyboard.class)
public abstract class KeyboardMixin {
    @Inject(method = "processF3", cancellable = true, at = @At("HEAD"))
    private void processF3_addCullvisKeys(int key, CallbackInfoReturnable<Boolean> cir) {
        switch (key) {
            case GLFW.GLFW_KEY_W:
                Pastry.captureCullingState();

                int numVisible =
                        Pastry.CAPTURED_CULLING_STATE != null ? Pastry.CAPTURED_CULLING_STATE.visible.size() : 0;
                sendChat(new LiteralText("Captured culling state (" + numVisible + " subchunks visible)"));

                cir.setReturnValue(true);
                return;
            case GLFW.GLFW_KEY_E:
                Pastry.toggleCullingVisualizer();

                sendChat(new LiteralText(
                        "Subchunk info display " + (Pastry.DISPLAY_CULLING_STATE ? "enabled" : "disabled")));

                cir.setReturnValue(true);
                return;
            case GLFW.GLFW_KEY_R:
                boolean captured = Pastry.toggleFrustumCapture();

                sendChat(new LiteralText((captured ? "Captured" : "Cleared") + " camera frustum"));

                cir.setReturnValue(true);
                return;
        }
    }

    @Inject(method = "processF3", at = @At("RETURN"))
    private void processF3_showCullvisKeys(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key != GLFW.GLFW_KEY_Q) {
            return;
        }

        @SuppressWarnings("resource")
        ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();

        chatHud.addMessage(new LiteralText("F3 + W = Capture visible chunks"));
        chatHud.addMessage(new LiteralText("F3 + E = Toggle subchunk info"));
        chatHud.addMessage(new LiteralText("F3 + R = Toggle frustum capture"));
        return;
    }

    private static void sendChat(Text text) {
        @SuppressWarnings("resource")
        ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();

        chatHud.addMessage(new LiteralText("")
                .append(new TranslatableText("debug.prefix").formatted(Formatting.YELLOW, Formatting.BOLD))
                .append(" ")
                .append(text));
    }
}
