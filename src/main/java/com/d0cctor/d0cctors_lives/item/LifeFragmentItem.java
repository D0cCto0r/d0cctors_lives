package com.d0cctor.d0cctors_lives.item;

import com.d0cctor.d0cctors_lives.system.LivesManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class LifeFragmentItem extends Item {
    public LifeFragmentItem(Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }

        boolean used = LivesManager.useLifeFragment(serverPlayer, stack);
        return used ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
    }
}
