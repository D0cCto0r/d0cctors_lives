package com.d0cctor.d0cctors_lives.network;

import com.d0cctor.d0cctors_lives.client.ClientLivesData;
import com.d0cctor.d0cctors_lives.client.ClientHudEvents;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class LivesNetwork {
    private LivesNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(LivesSyncPayload.TYPE, LivesSyncPayload.STREAM_CODEC, LivesNetwork::handleSync);
        registrar.playToClient(LivesAllSyncPayload.TYPE, LivesAllSyncPayload.STREAM_CODEC, LivesNetwork::handleAllSync);
        registrar.playToClient(LifeLossEffectPayload.TYPE, LifeLossEffectPayload.STREAM_CODEC, LivesNetwork::handleLifeLossEffect);
    }

    private static void handleSync(LivesSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientLivesData.update(payload));
    }

    private static void handleAllSync(LivesAllSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientLivesData.updateAll(payload));
    }

    private static void handleLifeLossEffect(LifeLossEffectPayload payload, IPayloadContext context) {
        context.enqueueWork(ClientHudEvents::triggerLifeLossEffect);
    }

}
