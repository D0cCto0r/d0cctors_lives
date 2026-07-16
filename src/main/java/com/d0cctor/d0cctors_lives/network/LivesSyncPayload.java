package com.d0cctor.d0cctors_lives.network;

import com.d0cctor.d0cctors_lives.D0cctorsLives;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LivesSyncPayload(
        int lives,
        int maxLives,
        boolean outOfLives,
        boolean inLimbo
) implements CustomPacketPayload {
    public static final Type<LivesSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(D0cctorsLives.MOD_ID, "lives_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LivesSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public LivesSyncPayload decode(RegistryFriendlyByteBuf buf) {
            return new LivesSyncPayload(
                    buf.readInt(),
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readBoolean()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, LivesSyncPayload payload) {
            buf.writeInt(payload.lives());
            buf.writeInt(payload.maxLives());
            buf.writeBoolean(payload.outOfLives());
            buf.writeBoolean(payload.inLimbo());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
