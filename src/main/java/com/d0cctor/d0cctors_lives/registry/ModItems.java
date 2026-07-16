package com.d0cctor.d0cctors_lives.registry;

import com.d0cctor.d0cctors_lives.D0cctorsLives;
import com.d0cctor.d0cctors_lives.item.LifeFragmentItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, D0cctorsLives.MOD_ID);

    public static final DeferredHolder<Item, Item> FRAGMENTO_DEL_CICLO = ITEMS.register("fragmento_del_ciclo", () ->
            new LifeFragmentItem(new Item.Properties().stacksTo(16))
    );

    // Item técnico usado solo para reproducir la animación vanilla tipo Totem con icono de corazón roto.
    // No se agrega a ninguna creative tab.
    public static final DeferredHolder<Item, Item> CORAZON_ROTO_ICON = ITEMS.register("corazon_roto_icon", () ->
            new Item(new Item.Properties().stacksTo(1))
    );

    private ModItems() {}
}
