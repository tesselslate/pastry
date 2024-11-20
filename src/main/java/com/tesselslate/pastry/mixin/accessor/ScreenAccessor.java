package com.tesselslate.pastry.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.item.ItemRenderer;

@Mixin(value = Screen.class)
public interface ScreenAccessor {
    @Accessor("itemRenderer")
    public ItemRenderer getItemRenderer();
}
