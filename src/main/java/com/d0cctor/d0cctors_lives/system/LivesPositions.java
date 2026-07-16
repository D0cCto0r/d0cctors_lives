package com.d0cctor.d0cctors_lives.system;

import com.d0cctor.d0cctors_lives.config.LivesConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivesPositions {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Data cached;

    public static Data get(MinecraftServer server) {
        if (cached == null) {
            cached = load(server);
        }
        return cached;
    }

    public static Pos getPos(MinecraftServer server, String key) {
        Data data = get(server);
        return switch (key) {
            case "limbo" -> data.limbo;
            case "town" -> data.town;
            case "deathspawn" -> data.deathspawn;
            default -> data.town;
        };
    }

    public static void savePlayerPos(MinecraftServer server, String key, ServerPlayer player) {
        Data data = get(server);
        Pos pos = new Pos(
                player.level().dimension().location().toString(),
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot()
        );

        switch (key) {
            case "limbo" -> data.limbo = pos;
            case "town" -> data.town = pos;
            case "deathspawn" -> data.deathspawn = pos;
        }

        save(server, data);
    }

    public static String posText(Pos pos) {
        return pos.dim + " " + (int)Math.floor(pos.x) + " " + (int)Math.floor(pos.y) + " " + (int)Math.floor(pos.z);
    }

    private static Data load(MinecraftServer server) {
        Path path = path(server);
        if (Files.exists(path)) {
            try {
                Data data = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), Data.class);
                if (data != null) return fixDefaults(data);
            } catch (Exception ignored) {}
        }

        Data data = defaults();
        save(server, data);
        return data;
    }

    private static Data fixDefaults(Data data) {
        Data def = defaults();
        if (data.limbo == null) data.limbo = def.limbo;
        if (data.town == null) data.town = def.town;
        if (data.deathspawn == null) data.deathspawn = def.deathspawn;
        return data;
    }

    private static void save(MinecraftServer server, Data data) {
        try {
            Path path = path(server);
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(data), StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    private static Path path(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("d0cctors_lives_positions.json");
    }

    private static Data defaults() {
        Data data = new Data();
        data.limbo = new Pos(LivesConfig.DEFAULT_LIMBO_DIM.get(), LivesConfig.DEFAULT_LIMBO_X.get(), LivesConfig.DEFAULT_LIMBO_Y.get(), LivesConfig.DEFAULT_LIMBO_Z.get(), 0.0F, 0.0F);
        data.town = new Pos(LivesConfig.DEFAULT_TOWN_DIM.get(), LivesConfig.DEFAULT_TOWN_X.get(), LivesConfig.DEFAULT_TOWN_Y.get(), LivesConfig.DEFAULT_TOWN_Z.get(), 0.0F, 0.0F);
        data.deathspawn = new Pos(LivesConfig.DEFAULT_DEATH_DIM.get(), LivesConfig.DEFAULT_DEATH_X.get(), LivesConfig.DEFAULT_DEATH_Y.get(), LivesConfig.DEFAULT_DEATH_Z.get(), 0.0F, 0.0F);
        return data;
    }

    public static final class Data {
        public Pos limbo;
        public Pos town;
        public Pos deathspawn;
    }

    public static final class Pos {
        public String dim;
        public double x;
        public double y;
        public double z;
        public float yaw;
        public float pitch;

        public Pos() {}

        public Pos(String dim, double x, double y, double z, float yaw, float pitch) {
            this.dim = dim;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    private LivesPositions() {}
}
