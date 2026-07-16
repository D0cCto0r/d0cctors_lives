package com.d0cctor.d0cctors_lives.system;

import com.d0cctor.d0cctors_lives.config.LivesConfig;
import com.d0cctor.d0cctors_lives.registry.ModItems;
import com.d0cctor.d0cctors_lives.network.LivesSyncPayload;
import com.d0cctor.d0cctors_lives.network.LivesAllSyncPayload;
import com.d0cctor.d0cctors_lives.network.LifeLossEffectPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

public final class LivesManager {
    public static final String OBJ_LIVES = "d0_lives";
    private static final String KEY_DATE = "d0LivesDate";
    private static final String KEY_LIVES = "d0Lives";
    private static final String KEY_OUT = "d0OutOfLives";
    private static final String KEY_LIMBO = "d0InLimbo";
    private static final String KEY_MSG = "d0LimboMessageShown";
    private static final String KEY_PENDING_EFFECT = "d0PendingLifeLossEffect";
    private static final Map<UUID, Integer> LIVE_CACHE = new HashMap<>();

    public static void setupServer(MinecraftServer server) {
        if (LivesConfig.KEEP_INVENTORY.get()) {
            run(server, "gamerule keepInventory true");
        }
        setupScores(server);
        LivesPositions.get(server);
    }

    public static void setupScores(MinecraftServer server) {
        run(server, "scoreboard objectives add " + OBJ_LIVES + " dummy");

        // No mostramos vidas en el TAB.
        // El objetivo queda solo para datos internos/network.
        run(server, "scoreboard objectives modify " + OBJ_LIVES + " rendertype integer");
        run(server, "scoreboard objectives setdisplay list");

        clearOldTeamSuffixes(server);
    }

    public static void setupTeams(MinecraftServer server) {
        // Legacy: los teams ponían corazones en TAB pero también aparecían en el chat.
        // Ahora no se usan. Solo limpiamos suffix viejos.
        clearOldTeamSuffixes(server);
    }

    private static void clearOldTeamSuffixes(MinecraftServer server) {
        // Limpia teams de versiones anteriores para que no queden corazones pegados al nombre en el chat.
        int max = Math.max(20, LivesConfig.maxLives());
        for (int lives = 0; lives <= max; lives++) {
            String team = teamName(lives);
            run(server, "team add " + team);
            run(server, "team modify " + team + " suffix \"\"");
        }
    }

    public static String suffixFor(int lives, int max) {
        if (lives <= 0) return " §8" + LivesConfig.DEAD_ICON.get();
        StringBuilder sb = new StringBuilder(" §c");
        String full = LivesConfig.FULL_HEART.get();
        String empty = LivesConfig.EMPTY_HEART.get();
        for (int i = 0; i < max; i++) {
            if (i < lives) sb.append(full);
            else if (i == lives) sb.append("§8").append(empty);
            else sb.append(empty);
        }
        return sb.toString();
    }

    public static void onLogin(ServerPlayer player) {
        setupServer(player.server);
        resetLivesIfNeeded(player);
        sync(player);

        if (isOutOfLives(player)) {
            applyZeroLives(player);
            return;
        }

        tell(player, "§7Despertares disponibles hoy: §c" + getLives(player) + "/" + LivesConfig.maxLives());
    }

    public static void onTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        resetLivesIfNeeded(player);
        sync(player);
        syncAllIfLeader(player);
        if (isOutOfLives(player)) applyZeroLivesTick(player);
    }

    public static void onDeath(ServerPlayer player) {
        resetLivesIfNeeded(player);
        if (isOutOfLives(player)) return;

        int lives = getLives(player) - 1;
        setLives(player, lives);

        if (lives <= 0) {
            setOutOfLives(player, true);
            run(player.server, "tellraw @a [{\"text\":\"☠ \",\"color\":\"dark_red\"},{\"text\":\"" + player.getGameProfile().getName() + "\",\"color\":\"red\",\"bold\":true},{\"text\":\" murió y agotó el ciclo. \",\"color\":\"gray\"},{\"text\":\"Ahora tiene 0/" + LivesConfig.maxLives() + " vidas.\",\"color\":\"dark_red\"}]");
            return;
        }

        // El efecto visual/sonido se reproduce al respawnear, no al morir.
        player.getPersistentData().putBoolean(KEY_PENDING_EFFECT, true);

        // No tocamos el spawn normal del jugador.
        // Si tiene cama respawnea en cama; si no, en el spawn vanilla del mundo.
        run(player.server, "tellraw @a [{\"text\":\"☠ \",\"color\":\"dark_red\"},{\"text\":\"" + player.getGameProfile().getName() + "\",\"color\":\"red\",\"bold\":true},{\"text\":\" murió y perdió 1 vida. \",\"color\":\"gray\"},{\"text\":\"Ahora tiene " + lives + "/" + LivesConfig.maxLives() + " vidas.\",\"color\":\"red\"}]");
    }

    public static void onRespawn(ServerPlayer player) {
        resetLivesIfNeeded(player);
        sync(player);
        if (isOutOfLives(player)) {
            applyZeroLives(player);
            return;
        }

        if (player.getPersistentData().getBoolean(KEY_PENDING_EFFECT)) {
            player.getPersistentData().putBoolean(KEY_PENDING_EFFECT, false);
            PacketDistributor.sendToPlayer(player, new LifeLossEffectPayload());
            run(player.server, "effect give " + player.getGameProfile().getName() + " minecraft:darkness 3 0 true");
            run(player.server, "effect give " + player.getGameProfile().getName() + " minecraft:slowness 3 0 true");
            run(player.server, "playsound minecraft:entity.warden.heartbeat master " + player.getGameProfile().getName() + " ~ ~ ~ 0.9 0.7");
            run(player.server, "playsound minecraft:block.sculk_shrieker.shriek master " + player.getGameProfile().getName() + " ~ ~ ~ 0.45 0.85");
            run(player.server, "playsound minecraft:block.respawn_anchor.deplete master " + player.getGameProfile().getName() + " ~ ~ ~ 0.8 0.65");
            run(player.server, "particle minecraft:sculk_soul " + player.getX() + " " + (player.getY() + 1.0D) + " " + player.getZ() + " 0.45 0.7 0.45 0.02 28 force " + player.getGameProfile().getName());
            run(player.server, "particle minecraft:damage_indicator " + player.getX() + " " + (player.getY() + 1.2D) + " " + player.getZ() + " 0.4 0.6 0.4 0.03 12 force " + player.getGameProfile().getName());
        }

        // No modificamos el spawn normal al respawnear con vidas.
    }

    public static void resetLivesIfNeeded(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        String today = todayKey();

        if (!data.contains(KEY_DATE)) {
            initLives(player);
            return;
        }

        if (LivesConfig.DAILY_RESET_ENABLED.get() && !today.equals(data.getString(KEY_DATE))) {
            boolean wasOut = data.getBoolean(KEY_OUT) || data.getBoolean(KEY_LIMBO);
            initLives(player);
            tell(player, "§aEl ciclo se restauró. §7Volvés a tener §c" + LivesConfig.maxLives() + "/" + LivesConfig.maxLives() + "§7 vidas.");
            if (wasOut) releaseFromLimbo(player);
            return;
        }

        if (!data.contains(KEY_LIVES)) data.putInt(KEY_LIVES, LivesConfig.maxLives());
        if (!data.contains(KEY_OUT)) data.putBoolean(KEY_OUT, false);
        if (!data.contains(KEY_LIMBO)) data.putBoolean(KEY_LIMBO, false);
        if (!data.contains(KEY_MSG)) data.putBoolean(KEY_MSG, false);
        if (!data.contains(KEY_PENDING_EFFECT)) data.putBoolean(KEY_PENDING_EFFECT, false);
    }

    public static void initLives(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.putString(KEY_DATE, todayKey());
        data.putInt(KEY_LIVES, Math.min(LivesConfig.STARTING_LIVES.get(), LivesConfig.maxLives()));
        data.putBoolean(KEY_OUT, false);
        data.putBoolean(KEY_LIMBO, false);
        data.putBoolean(KEY_MSG, false);
        data.putBoolean(KEY_PENDING_EFFECT, false);
        LIVE_CACHE.put(player.getUUID(), Math.min(LivesConfig.STARTING_LIVES.get(), LivesConfig.maxLives()));
        run(player.server, "gamemode survival " + player.getGameProfile().getName());
        sync(player);
    }

    public static int getLives(ServerPlayer player) {
        resetIfMissingOnly(player);
        UUID id = player.getUUID();
        if (LIVE_CACHE.containsKey(id)) {
            int cached = LIVE_CACHE.get(id);
            return Math.max(0, Math.min(LivesConfig.maxLives(), cached));
        }
        int lives = player.getPersistentData().getInt(KEY_LIVES);
        lives = Math.max(0, Math.min(LivesConfig.maxLives(), lives));
        LIVE_CACHE.put(id, lives);
        return lives;
    }

    private static void resetIfMissingOnly(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(KEY_DATE)) data.putString(KEY_DATE, todayKey());
        if (!data.contains(KEY_LIVES)) data.putInt(KEY_LIVES, LivesConfig.maxLives());
        if (!data.contains(KEY_OUT)) data.putBoolean(KEY_OUT, false);
        if (!data.contains(KEY_LIMBO)) data.putBoolean(KEY_LIMBO, false);
        if (!data.contains(KEY_MSG)) data.putBoolean(KEY_MSG, false);
        if (!data.contains(KEY_PENDING_EFFECT)) data.putBoolean(KEY_PENDING_EFFECT, false);
    }

    public static void setLives(ServerPlayer player, int amount) {
        int max = LivesConfig.maxLives();
        int lives = Math.max(0, Math.min(max, amount));
        CompoundTag data = player.getPersistentData();
        data.putString(KEY_DATE, todayKey());
        data.putInt(KEY_LIVES, lives);
        LIVE_CACHE.put(player.getUUID(), lives);

        if (lives > 0) {
            data.putBoolean(KEY_OUT, false);
            data.putBoolean(KEY_LIMBO, false);
            data.putBoolean(KEY_MSG, false);
            data.putBoolean(KEY_PENDING_EFFECT, false);
        } else {
            data.putBoolean(KEY_OUT, true);
        }
        sync(player);
    }

    public static boolean isOutOfLives(ServerPlayer player) {
        resetIfMissingOnly(player);
        return player.getPersistentData().getBoolean(KEY_OUT);
    }

    public static boolean isInLimbo(ServerPlayer player) {
        resetIfMissingOnly(player);
        return player.getPersistentData().getBoolean(KEY_LIMBO);
    }

    public static void setOutOfLives(ServerPlayer player, boolean value) {
        player.getPersistentData().putBoolean(KEY_OUT, value);
    }

    public static void sendToLimbo(ServerPlayer player) {
        setLives(player, 0);
        player.getPersistentData().putBoolean(KEY_OUT, true);
        player.getPersistentData().putBoolean(KEY_LIMBO, true);

        LivesPositions.Pos limbo = LivesPositions.getPos(player.server, "limbo");
        tpToPos(player, limbo);
        run(player.server, "gamemode adventure " + player.getGameProfile().getName());

        if (!player.getPersistentData().getBoolean(KEY_MSG)) {
            tell(player, "§8Volviste al vacío. §7El Observador te espera.");
            player.getPersistentData().putBoolean(KEY_MSG, true);
        }
        applyLimboState(player);
    }

    public static void releaseFromLimbo(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(KEY_OUT, false);
        data.putBoolean(KEY_LIMBO, false);
        data.putBoolean(KEY_MSG, false);
        run(player.server, "gamemode survival " + player.getGameProfile().getName());
        teleportToNormalSpawn(player);
        tell(player, "§aEl ciclo volvió a abrirse.");
    }

    public static void applyZeroLives(ServerPlayer player) {
        switch (LivesConfig.zeroLivesAction()) {
            case "spectator" -> run(player.server, "gamemode spectator " + player.getGameProfile().getName());
            case "ban" -> run(player.server, "ban " + player.getGameProfile().getName() + " Sin vidas restantes.");
            default -> sendToLimbo(player);
        }
    }

    public static void applyZeroLivesTick(ServerPlayer player) {
        switch (LivesConfig.zeroLivesAction()) {
            case "spectator" -> run(player.server, "gamemode spectator " + player.getGameProfile().getName());
            case "ban" -> {}
            default -> applyLimboState(player);
        }
    }

    public static void applyLimboState(ServerPlayer player) {
        LivesPositions.Pos limbo = LivesPositions.getPos(player.server, "limbo");
        run(player.server, "gamemode adventure " + player.getGameProfile().getName());
        run(player.server, "effect give " + player.getGameProfile().getName() + " minecraft:resistance 3 255 true");
        run(player.server, "effect give " + player.getGameProfile().getName() + " minecraft:regeneration 3 255 true");
        run(player.server, "effect give " + player.getGameProfile().getName() + " minecraft:fire_resistance 3 255 true");
        run(player.server, "effect give " + player.getGameProfile().getName() + " minecraft:water_breathing 3 255 true");
        run(player.server, "effect give " + player.getGameProfile().getName() + " minecraft:saturation 3 255 true");
        if (player.getY() < limbo.y - 10.0D) tpToPos(player, limbo);
    }

    public static void copyLivesData(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        CompoundTag oldData = oldPlayer.getPersistentData();
        CompoundTag newData = newPlayer.getPersistentData();

        if (oldData.contains(KEY_DATE)) newData.putString(KEY_DATE, oldData.getString(KEY_DATE));
        if (oldData.contains(KEY_LIVES)) newData.putInt(KEY_LIVES, oldData.getInt(KEY_LIVES));
        if (oldData.contains(KEY_OUT)) newData.putBoolean(KEY_OUT, oldData.getBoolean(KEY_OUT));
        if (oldData.contains(KEY_LIMBO)) newData.putBoolean(KEY_LIMBO, oldData.getBoolean(KEY_LIMBO));
        if (oldData.contains(KEY_MSG)) newData.putBoolean(KEY_MSG, oldData.getBoolean(KEY_MSG));
        if (oldData.contains(KEY_PENDING_EFFECT)) newData.putBoolean(KEY_PENDING_EFFECT, oldData.getBoolean(KEY_PENDING_EFFECT));
        if (newData.contains(KEY_LIVES)) {
            LIVE_CACHE.put(newPlayer.getUUID(), Math.max(0, Math.min(LivesConfig.maxLives(), newData.getInt(KEY_LIVES))));
        }
    }

    public static boolean useLifeFragment(ServerPlayer player, ItemStack stack) {
        resetLivesIfNeeded(player);
        int lives = getLives(player);
        int max = LivesConfig.maxLives();
        if (lives >= max) {
            tell(player, "§7Tu ciclo ya está completo. §cNo podés superar " + max + "/" + max + " vidas.");
            return false;
        }

        int newLives = lives + 1;
        setLives(player, newLives);
        if (!player.isCreative()) stack.shrink(1);

        run(player.server, "playsound minecraft:item.totem.use master " + player.getGameProfile().getName() + " ~ ~ ~ 0.8 1.15");
        run(player.server, "playsound minecraft:block.respawn_anchor.charge master " + player.getGameProfile().getName() + " ~ ~ ~ 1.0 1.05");
        run(player.server, "particle minecraft:reverse_portal " + player.getX() + " " + (player.getY() + 1.0D) + " " + player.getZ() + " 0.4 0.8 0.4 0.02 30 force " + player.getGameProfile().getName());

        if (lives <= 0 || isInLimbo(player)) {
            releaseFromLimbo(player);
        }

        // Solo mensaje global, para que no haya doble texto.
        run(player.server, "tellraw @a [{\"text\":\"✦ \",\"color\":\"dark_red\"},{\"text\":\"" + player.getGameProfile().getName() + "\",\"color\":\"red\",\"bold\":true},{\"text\":\" usó un \",\"color\":\"gray\"},{\"text\":\"Fragmento del Ciclo\",\"color\":\"dark_red\",\"bold\":true},{\"text\":\" y restauró 1 vida. \",\"color\":\"gray\"},{\"text\":\"Ahora tiene " + newLives + "/" + max + " vidas.\",\"color\":\"red\"}]");
        return true;
    }

    public static void sync(ServerPlayer player) {
        int lives = getLives(player);
        MinecraftServer server = player.server;
        setupScores(server);

        // Score real. El scoreboard vanilla muestra número, pero el cliente lo tapa
        // y dibuja corazones custom en su lugar.
        run(server, "scoreboard players set " + player.getGameProfile().getName() + " " + OBJ_LIVES + " " + lives);

        // TAB vanilla: d0_lives ya está visible en la lista.
        // Enviamos las vidas reales al cliente para HUD y animaciones.
        PacketDistributor.sendToPlayer(player, new LivesSyncPayload(
                lives,
                LivesConfig.maxLives(),
                isOutOfLives(player),
                isInLimbo(player)
        ));
    }

    public static void syncAll(MinecraftServer server) {
        List<String> names = new ArrayList<>();
        List<Integer> lives = new ArrayList<>();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            names.add(player.getGameProfile().getName());
            lives.add(getLives(player));
        }

        LivesAllSyncPayload payload = new LivesAllSyncPayload(names, lives, LivesConfig.maxLives());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    private static void syncAllIfLeader(ServerPlayer player) {
        List<ServerPlayer> players = player.server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;
        if (players.get(0).getUUID().equals(player.getUUID())) {
            syncAll(player.server);
        }
    }

    private static String teamName(int lives) {
        return "d0_lives_" + Math.max(0, Math.min(LivesConfig.maxLives(), lives));
    }

    public static String todayKey() {
        LocalDateTime arg = LocalDateTime.now(ZoneOffset.UTC).plusHours(-3);
        int resetHour = LivesConfig.DAILY_RESET_HOUR_ARG.get();
        int resetMinute = LivesConfig.DAILY_RESET_MINUTE_ARG.get();
        if (arg.getHour() < resetHour || (arg.getHour() == resetHour && arg.getMinute() < resetMinute)) {
            arg = arg.minusDays(1);
        }
        return arg.getYear() + "-" + arg.getMonthValue() + "-" + arg.getDayOfMonth();
    }

    public static void teleportToNormalSpawn(ServerPlayer player) {
        try {
            MinecraftServer server = player.server;
            ResourceKey<Level> respawnDimension = player.getRespawnDimension();
            BlockPos respawnPosition = player.getRespawnPosition();

            ServerLevel level = server.getLevel(respawnDimension);
            if (level == null) level = server.overworld();

            if (respawnPosition != null) {
                // Compilación segura para 1.21.1:
                // en esta versión el helper exacto de cama cambia de nombre/mapeo,
                // así que usamos directamente la posición de respawn guardada del jugador.
                player.teleportTo(
                        level,
                        respawnPosition.getX() + 0.5D,
                        respawnPosition.getY() + 0.1D,
                        respawnPosition.getZ() + 0.5D,
                        player.getRespawnAngle(),
                        player.getXRot()
                );
                return;
            }

            ServerLevel overworld = server.overworld();
            BlockPos shared = overworld.getSharedSpawnPos();
            player.teleportTo(
                    overworld,
                    shared.getX() + 0.5D,
                    shared.getY(),
                    shared.getZ() + 0.5D,
                    0.0F,
                    player.getXRot()
            );
        } catch (Exception e) {
            LivesPositions.Pos town = LivesPositions.getPos(player.server, "town");
            tpToPos(player, town);
        }
    }

    public static void tpToPos(ServerPlayer player, LivesPositions.Pos pos) {
        run(player.server, "execute in " + pos.dim + " run tp " + player.getGameProfile().getName() + " " + pos.x + " " + pos.y + " " + pos.z + " " + pos.yaw + " " + pos.pitch);
    }

    public static void spawnpointToPos(ServerPlayer player, LivesPositions.Pos pos) {
        run(player.server, "execute in " + pos.dim + " run spawnpoint " + player.getGameProfile().getName() + " " + (int)Math.floor(pos.x) + " " + (int)Math.floor(pos.y) + " " + (int)Math.floor(pos.z));
    }

    public static void tell(ServerPlayer player, String message) {
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
    }

    public static void run(MinecraftServer server, String command) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
    }

    private LivesManager() {}
}
