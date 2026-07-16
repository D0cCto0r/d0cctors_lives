package com.d0cctor.d0cctors_lives;

import com.d0cctor.d0cctors_lives.commands.LivesCommands;
import com.d0cctor.d0cctors_lives.config.LivesConfig;
import com.d0cctor.d0cctors_lives.events.LivesEvents;
import com.d0cctor.d0cctors_lives.registry.ModItems;
import com.d0cctor.d0cctors_lives.network.LivesNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(D0cctorsLives.MOD_ID)
public final class D0cctorsLives {
    public static final String MOD_ID = "d0cctors_lives";

    public D0cctorsLives(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(LivesNetwork::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, LivesConfig.SPEC);

        NeoForge.EVENT_BUS.register(new LivesEvents());
        NeoForge.EVENT_BUS.addListener(LivesCommands::onRegisterCommands);
    }
}
