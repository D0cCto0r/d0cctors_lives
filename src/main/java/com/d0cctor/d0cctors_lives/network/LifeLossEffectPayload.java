package com.d0cctor.d0cctors_lives.network;

import com.d0cctor.d0cctors_lives.D0cctorsLives;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LifeLossEffectPayload() implements CustomPacketPayload {
    public static final Type<LifeLossEffectPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(D0cctorsLives.MOD_ID, "life_loss_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LifeLossEffectPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public LifeLossEffectPayload decode(RegistryFriendlyByteBuf buf) {
            return new LifeLossEffectPayload();
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, LifeLossEffectPayload payload) {
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
