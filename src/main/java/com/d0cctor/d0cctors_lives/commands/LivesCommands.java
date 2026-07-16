package com.d0cctor.d0cctors_lives.commands;

import com.d0cctor.d0cctors_lives.config.LivesConfig;
import com.d0cctor.d0cctors_lives.system.LivesManager;
import com.d0cctor.d0cctors_lives.system.LivesPositions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class LivesCommands {
    private LivesCommands() {}

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vidas")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reset")
                        .then(Commands.argument("jugador", EntityArgument.player())
                                .executes(ctx -> reset(ctx.getSource(), EntityArgument.getPlayer(ctx, "jugador")))))
                .then(Commands.literal("ver")
                        .then(Commands.argument("jugador", EntityArgument.player())
                                .executes(ctx -> view(ctx.getSource(), EntityArgument.getPlayer(ctx, "jugador")))))
                .then(Commands.literal("set")
                        .then(Commands.argument("jugador", EntityArgument.player())
                                .then(Commands.argument("cantidad", IntegerArgumentType.integer(0, 20))
                                        .executes(ctx -> set(ctx.getSource(), EntityArgument.getPlayer(ctx, "jugador"), IntegerArgumentType.getInteger(ctx, "cantidad"))))))
                .then(Commands.literal("limbo")
                        .then(Commands.argument("jugador", EntityArgument.player())
                                .executes(ctx -> limbo(ctx.getSource(), EntityArgument.getPlayer(ctx, "jugador")))))
                .then(Commands.literal("liberar")
                        .then(Commands.argument("jugador", EntityArgument.player())
                                .executes(ctx -> release(ctx.getSource(), EntityArgument.getPlayer(ctx, "jugador")))))
                .then(Commands.literal("setlimbo")
                        .executes(ctx -> savePos(ctx.getSource(), "limbo")))
                .then(Commands.literal("settown")
                        .executes(ctx -> savePos(ctx.getSource(), "town")))
                .then(Commands.literal("setdeathspawn")
                        .executes(ctx -> savePos(ctx.getSource(), "deathspawn")))
                .then(Commands.literal("tp")
                        .then(Commands.literal("limbo").executes(ctx -> tp(ctx.getSource(), "limbo")))
                        .then(Commands.literal("town").executes(ctx -> tp(ctx.getSource(), "town")))
                        .then(Commands.literal("deathspawn").executes(ctx -> tp(ctx.getSource(), "deathspawn"))))
                .then(Commands.literal("config")
                        .executes(ctx -> config(ctx.getSource())))
        );

        dispatcher.register(Commands.literal("lives")
                .executes(ctx -> self(ctx.getSource()))
        );
    }

    private static int self(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            LivesManager.resetLivesIfNeeded(player);
            source.sendSuccess(() -> Component.literal("✦ Vidas: " + LivesManager.getLives(player) + "/" + LivesConfig.maxLives()).withStyle(ChatFormatting.RED), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Este comando solo puede usarlo un jugador."));
            return 0;
        }
    }

    private static int reset(CommandSourceStack source, ServerPlayer target) {
        LivesManager.initLives(target);
        target.sendSystemMessage(Component.literal("§aTus vidas fueron reseteadas: §f" + LivesConfig.maxLives() + "/" + LivesConfig.maxLives()));
        source.sendSuccess(() -> Component.literal("Vidas reseteadas para " + target.getGameProfile().getName()), true);
        return 1;
    }

    private static int view(CommandSourceStack source, ServerPlayer target) {
        LivesManager.resetLivesIfNeeded(target);
        source.sendSuccess(() -> Component.literal("Jugador: " + target.getGameProfile().getName()), false);
        source.sendSuccess(() -> Component.literal("Vidas: " + LivesManager.getLives(target) + "/" + LivesConfig.maxLives()), false);
        source.sendSuccess(() -> Component.literal("Sin vidas: " + LivesManager.isOutOfLives(target)), false);
        source.sendSuccess(() -> Component.literal("En limbo: " + LivesManager.isInLimbo(target)), false);
        source.sendSuccess(() -> Component.literal("Modo 0 vidas: " + LivesConfig.zeroLivesAction()), false);
        return 1;
    }

    private static int set(CommandSourceStack source, ServerPlayer target, int amount) {
        boolean wasOut = LivesManager.isOutOfLives(target) || LivesManager.isInLimbo(target);
        LivesManager.setLives(target, amount);
        if (amount <= 0) LivesManager.applyZeroLives(target);
        else if (wasOut) LivesManager.releaseFromLimbo(target);
        target.sendSystemMessage(Component.literal("§aTus vidas fueron ajustadas a: §f" + LivesManager.getLives(target) + "/" + LivesConfig.maxLives()));
        source.sendSuccess(() -> Component.literal("Vidas ajustadas para " + target.getGameProfile().getName()), true);
        return 1;
    }

    private static int limbo(CommandSourceStack source, ServerPlayer target) {
        LivesManager.sendToLimbo(target);
        target.sendSystemMessage(Component.literal("§8Fuiste enviado al vacío."));
        source.sendSuccess(() -> Component.literal("Enviado al limbo: " + target.getGameProfile().getName()), true);
        return 1;
    }

    private static int release(CommandSourceStack source, ServerPlayer target) {
        LivesManager.setLives(target, LivesConfig.maxLives());
        LivesManager.releaseFromLimbo(target);
        target.sendSystemMessage(Component.literal("§aFuiste liberado del vacío. Vidas: §f" + LivesConfig.maxLives() + "/" + LivesConfig.maxLives()));
        source.sendSuccess(() -> Component.literal("Liberado del limbo: " + target.getGameProfile().getName()), true);
        return 1;
    }

    private static int savePos(CommandSourceStack source, String key) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            LivesPositions.savePlayerPos(player.server, key, player);
            LivesPositions.Pos pos = LivesPositions.getPos(player.server, key);
            source.sendSuccess(() -> Component.literal("Posición guardada: " + key + " = " + LivesPositions.posText(pos)), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Este comando solo puede usarlo un jugador."));
            return 0;
        }
    }

    private static int tp(CommandSourceStack source, String key) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            LivesManager.tpToPos(player, LivesPositions.getPos(player.server, key));
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Este comando solo puede usarlo un jugador."));
            return 0;
        }
    }

    private static int config(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            LivesPositions.Pos limbo = LivesPositions.getPos(player.server, "limbo");
            LivesPositions.Pos town = LivesPositions.getPos(player.server, "town");
            LivesPositions.Pos death = LivesPositions.getPos(player.server, "deathspawn");
            player.sendSystemMessage(Component.literal("§6===== Config Vidas ====="));
            player.sendSystemMessage(Component.literal("§8Limbo: §f" + LivesPositions.posText(limbo)));
            player.sendSystemMessage(Component.literal("§aTown: §f" + LivesPositions.posText(town)));
            player.sendSystemMessage(Component.literal("§cDeathSpawn: §f" + LivesPositions.posText(death)));
            player.sendSystemMessage(Component.literal("§7Vidas máximas: §c" + LivesConfig.maxLives()));
            player.sendSystemMessage(Component.literal("§7Modo 0 vidas: §f" + LivesConfig.zeroLivesAction()));
            player.sendSystemMessage(Component.literal("§7Reset diario: §f" + LivesConfig.DAILY_RESET_HOUR_ARG.get() + ":" + String.format("%02d", LivesConfig.DAILY_RESET_MINUTE_ARG.get()) + " ARG"));
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Este comando solo puede usarlo un jugador."));
            return 0;
        }
    }
}
