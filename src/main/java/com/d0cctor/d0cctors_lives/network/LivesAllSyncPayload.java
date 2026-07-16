package com.d0cctor.d0cctors_lives.network;

import com.d0cctor.d0cctors_lives.D0cctorsLives;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record LivesAllSyncPayload(
        List<String> names,
        List<Integer> lives,
        int maxLives
) implements CustomPacketPayload {
    public static final Type<LivesAllSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(D0cctorsLives.MOD_ID, "lives_all_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LivesAllSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public LivesAllSyncPayload decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readInt();
            List<String> names = new ArrayList<>();
            List<Integer> lives = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                names.add(buf.readUtf());
                lives.add(buf.readInt());
            }
            int maxLives = buf.readInt();
            return new LivesAllSyncPayload(names, lives, maxLives);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, LivesAllSyncPayload payload) {
            int size = Math.min(payload.names().size(), payload.lives().size());
            buf.writeInt(size);
            for (int i = 0; i < size; i++) {
                buf.writeUtf(payload.names().get(i));
                buf.writeInt(payload.lives().get(i));
            }
            buf.writeInt(payload.maxLives());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
