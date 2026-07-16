package com.d0cctor.d0cctors_lives.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class LivesConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MAX_LIVES;
    public static final ModConfigSpec.IntValue STARTING_LIVES;
    public static final ModConfigSpec.BooleanValue DAILY_RESET_ENABLED;
    public static final ModConfigSpec.IntValue DAILY_RESET_HOUR_ARG;
    public static final ModConfigSpec.IntValue DAILY_RESET_MINUTE_ARG;
    public static final ModConfigSpec.ConfigValue<String> ZERO_LIVES_ACTION;
    public static final ModConfigSpec.BooleanValue KEEP_INVENTORY;
    public static final ModConfigSpec.BooleanValue TAB_HEARTS_ENABLED;
    public static final ModConfigSpec.BooleanValue HUD_ENABLED;
    public static final ModConfigSpec.IntValue HUD_X;
    public static final ModConfigSpec.IntValue HUD_Y;
    public static final ModConfigSpec.ConfigValue<String> FULL_HEART;
    public static final ModConfigSpec.ConfigValue<String> EMPTY_HEART;
    public static final ModConfigSpec.ConfigValue<String> DEAD_ICON;

    public static final ModConfigSpec.ConfigValue<String> DEFAULT_LIMBO_DIM;
    public static final ModConfigSpec.DoubleValue DEFAULT_LIMBO_X;
    public static final ModConfigSpec.DoubleValue DEFAULT_LIMBO_Y;
    public static final ModConfigSpec.DoubleValue DEFAULT_LIMBO_Z;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_TOWN_DIM;
    public static final ModConfigSpec.DoubleValue DEFAULT_TOWN_X;
    public static final ModConfigSpec.DoubleValue DEFAULT_TOWN_Y;
    public static final ModConfigSpec.DoubleValue DEFAULT_TOWN_Z;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_DEATH_DIM;
    public static final ModConfigSpec.DoubleValue DEFAULT_DEATH_X;
    public static final ModConfigSpec.DoubleValue DEFAULT_DEATH_Y;
    public static final ModConfigSpec.DoubleValue DEFAULT_DEATH_Z;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");
        MAX_LIVES = builder.comment("Vidas máximas diarias.").defineInRange("max_lives", 4, 1, 20);
        STARTING_LIVES = builder.comment("Vidas iniciales para jugadores nuevos.").defineInRange("starting_lives", 4, 1, 20);
        DAILY_RESET_ENABLED = builder.define("daily_reset_enabled", true);
        DAILY_RESET_HOUR_ARG = builder.comment("Hora Argentina del reset diario. 1 = 1 AM Argentina.").defineInRange("daily_reset_hour_argentina", 1, 0, 23);
        DAILY_RESET_MINUTE_ARG = builder.defineInRange("daily_reset_minute_argentina", 0, 0, 59);
        ZERO_LIVES_ACTION = builder.comment("Qué pasa con 0 vidas: limbo, spectator o ban.").define("zero_lives_action", "limbo");
        KEEP_INVENTORY = builder.define("force_keep_inventory", true);
        builder.pop();

        builder.push("limbo_defaults");
        DEFAULT_LIMBO_DIM = builder.define("limbo_dimension", "minecraft:overworld");
        DEFAULT_LIMBO_X = builder.defineInRange("limbo_x", 0.0, -30000000.0, 30000000.0);
        DEFAULT_LIMBO_Y = builder.defineInRange("limbo_y", 100.0, -2048.0, 4096.0);
        DEFAULT_LIMBO_Z = builder.defineInRange("limbo_z", 0.0, -30000000.0, 30000000.0);
        DEFAULT_TOWN_DIM = builder.define("town_dimension", "minecraft:overworld");
        DEFAULT_TOWN_X = builder.defineInRange("town_x", 100.0, -30000000.0, 30000000.0);
        DEFAULT_TOWN_Y = builder.defineInRange("town_y", 65.0, -2048.0, 4096.0);
        DEFAULT_TOWN_Z = builder.defineInRange("town_z", 100.0, -30000000.0, 30000000.0);
        DEFAULT_DEATH_DIM = builder.define("deathspawn_dimension", "minecraft:overworld");
        DEFAULT_DEATH_X = builder.defineInRange("deathspawn_x", 100.0, -30000000.0, 30000000.0);
        DEFAULT_DEATH_Y = builder.defineInRange("deathspawn_y", 65.0, -2048.0, 4096.0);
        DEFAULT_DEATH_Z = builder.defineInRange("deathspawn_z", 100.0, -30000000.0, 30000000.0);
        builder.pop();

        builder.push("tab");
        TAB_HEARTS_ENABLED = builder.define("enabled", true);
        FULL_HEART = builder.define("full_heart", "❤");
        EMPTY_HEART = builder.define("empty_heart", "♡");
        DEAD_ICON = builder.define("dead_icon", "☠");
        builder.pop();

        builder.push("hud");
        HUD_ENABLED = builder.define("enabled", true);
        HUD_X = builder.defineInRange("x", 8, 0, 10000);
        HUD_Y = builder.defineInRange("y", 8, 0, 10000);
        builder.pop();

        SPEC = builder.build();
    }

    public static int maxLives() {
        return Math.max(1, MAX_LIVES.get());
    }

    public static String zeroLivesAction() {
        String value = ZERO_LIVES_ACTION.get().trim().toLowerCase();
        if (!value.equals("limbo") && !value.equals("spectator") && !value.equals("ban")) return "limbo";
        return value;
    }

    private LivesConfig() {}
}
