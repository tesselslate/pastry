package com.tesselslate.pastry.mixin.accessor;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.item.ItemRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Screen.class)
public interface ScreenAccessor {
    @Accessor("itemRenderer")
    public ItemRenderer getItemRenderer();
}
