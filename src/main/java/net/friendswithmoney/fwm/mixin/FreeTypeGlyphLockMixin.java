/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.mojang.blaze3d.font.TrueTypeGlyphProvider
 *  com.mojang.blaze3d.font.UnbakedGlyph
 *  net.minecraft.client.gui.font.providers.FreeTypeUtil
 *  org.lwjgl.util.freetype.FT_Face
 *  org.spongepowered.asm.mixin.Mixin
 */
package net.friendswithmoney.fwm.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.font.UnbakedGlyph;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={TrueTypeGlyphProvider.class})
public abstract class FreeTypeGlyphLockMixin {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"loadGlyph"})
    private UnbakedGlyph fwm$lockedLoad(int codePoint, FT_Face face, int index, Operation<UnbakedGlyph> original) {
        Object object = FreeTypeUtil.LIBRARY_LOCK;
        synchronized (object) {
            return (UnbakedGlyph)original.call(new Object[]{codePoint, face, index});
        }
    }
}

