package com.d0cctor.d0cctors_lives.client;

import com.d0cctor.d0cctors_lives.D0cctorsLives;
import com.d0cctor.d0cctors_lives.config.LivesConfig;
import com.d0cctor.d0cctors_lives.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;


@EventBusSubscriber(modid = D0cctorsLives.MOD_ID, value = Dist.CLIENT)
public final class ClientHudEvents {
    private static final ResourceLocation HEART_FULL = ResourceLocation.fromNamespaceAndPath(D0cctorsLives.MOD_ID, "textures/gui/vida_llena.png");
    private static final ResourceLocation HEART_EMPTY = ResourceLocation.fromNamespaceAndPath(D0cctorsLives.MOD_ID, "textures/gui/vida_rota.png");
    private static final String SCOREBOARD_OBJECTIVE = "d0_lives";
    private static int lastLives = -1;
    private static int brokenHeartTicks = 0;

    private ClientHudEvents() {}

    public static void triggerLifeLossEffect() {
        brokenHeartTicks = 0;
        lastLives = ClientLivesData.lives();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer != null) {
            minecraft.gameRenderer.displayItemActivation(new ItemStack(ModItems.CORAZON_ROTO_ICON.get()));
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!LivesConfig.HUD_ENABLED.get()) return;

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui) return;

        int lives = readLives(player);
        if (lives < 0) return;

        lastLives = lives;

        int maxLives = ClientLivesData.maxLives();
        int x = LivesConfig.HUD_X.get();
        int y = LivesConfig.HUD_Y.get();
        int size = 12;
        int spacing = 14;

        for (int i = 0; i < maxLives; i++) {
            ResourceLocation tex = i < lives ? HEART_FULL : HEART_EMPTY;
            event.getGuiGraphics().blit(tex, x + (i * spacing), y, 0.0F, 0.0F, size, size, size, size);
        }

        renderBrokenHeartPop(event, minecraft);
        if (brokenHeartTicks > 0) {
            brokenHeartTicks--;
        }
    }

    private static void renderBrokenHeartPop(RenderGuiEvent.Post event, Minecraft minecraft) {
        // La animación real ahora es la vanilla de item activation, usando el icono de corazón roto.
    }

    private static int readLives(LocalPlayer player) {
        if (!ClientLivesData.hasData()) return -1;
        int lives = ClientLivesData.lives();
        int max = ClientLivesData.maxLives();
        if (lives < 0) lives = 0;
        if (lives > max) lives = max;
        return lives;
    }



}
