/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.gui.font.providers.FreeTypeUtil
 *  org.lwjgl.util.freetype.FT_Face
 *  org.spongepowered.asm.mixin.Mixin
 */
package net.friendswithmoney.fwm.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={NativeImage.class})
public abstract class FreeTypeBitmapLockMixin {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"copyFromFont"})
    private boolean fwm$lockedCopy(FT_Face face, int index, Operation<Boolean> original) {
        Object object = FreeTypeUtil.LIBRARY_LOCK;
        synchronized (object) {
            return (Boolean)original.call(new Object[]{face, index});
        }
    }
}

