package com.d0cctor.d0cctors_lives.events;

import com.d0cctor.d0cctors_lives.registry.ModItems;
import com.d0cctor.d0cctors_lives.system.LivesManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class LivesEvents {
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        LivesManager.setupServer(event.getServer());
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LivesManager.onLogin(player);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LivesManager.onTick(player);
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LivesManager.onDeath(player);
        }
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()
                && event.getOriginal() instanceof ServerPlayer oldPlayer
                && event.getEntity() instanceof ServerPlayer newPlayer) {
            LivesManager.copyLivesData(oldPlayer, newPlayer);
        }
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LivesManager.onRespawn(player);
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        LivesManager.resetLivesIfNeeded(player);
        if (LivesManager.isOutOfLives(player)) {
            event.setCanceled(true);
            LivesManager.tell(player, "§8No podés interactuar con el vacío.");
        }
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        LivesManager.resetLivesIfNeeded(player);
        if (LivesManager.isOutOfLives(player) && !event.getItemStack().is(ModItems.FRAGMENTO_DEL_CICLO.get())) {
            event.setCanceled(true);
            LivesManager.tell(player, "§8No podés usar objetos en el vacío.");
        }
    }
}
