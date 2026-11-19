/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.PlayerFaceExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.resources.DefaultPlayerSkin
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.player.PlayerSkin
 *  net.minecraft.world.item.component.ResolvableProfile
 */
package net.friendswithmoney.fwm.ui;

import com.mojang.authlib.GameProfile;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;

public final class FwmGfx {
    private static final Map<String, ResolvableProfile> PROFILES = new ConcurrentHashMap<String, ResolvableProfile>();
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

    private FwmGfx() {
    }

    public static void blit(GuiGraphicsExtractor ctx, Identifier texture, int x, int y, int drawWidth, int drawHeight, int regionWidth, int regionHeight, int sheetSize) {
        ctx.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0f, 0.0f, drawWidth, drawHeight, regionWidth, regionHeight, sheetSize, sheetSize);
    }

    public static void blitAt(GuiGraphicsExtractor ctx, Identifier texture, int x, int y, int drawWidth, int drawHeight, float v, int regionWidth, int regionHeight, int sheetW, int sheetH) {
        ctx.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0f, v, drawWidth, drawHeight, regionWidth, regionHeight, sheetW, sheetH);
    }

    public static void blit(GuiGraphicsExtractor ctx, Identifier texture, int x, int y, int drawWidth, int drawHeight, int regionWidth, int regionHeight, int sheetW, int sheetH) {
        ctx.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0f, 0.0f, drawWidth, drawHeight, regionWidth, regionHeight, sheetW, sheetH);
    }

    public static void playerHead(GuiGraphicsExtractor ctx, int x, int y, int size) {
        PlayerFaceExtractor.extractRenderState((GuiGraphicsExtractor)ctx, (PlayerSkin)FwmGfx.skin(), (int)x, (int)y, (int)size);
    }

    public static void playerHead(GuiGraphicsExtractor ctx, String name, int x, int y, int size) {
        PlayerFaceExtractor.extractRenderState((GuiGraphicsExtractor)ctx, (PlayerSkin)FwmGfx.skinOf(name), (int)x, (int)y, (int)size);
    }

    public static Identifier skinTexture(String name) {
        return FwmGfx.skinOf(name).body().texturePath();
    }

    private static PlayerSkin skinOf(String name) {
        if (name == null || !NAME.matcher(name).matches()) {
            return DefaultPlayerSkin.getDefaultSkin();
        }
        ResolvableProfile profile = PROFILES.computeIfAbsent(name.toLowerCase(Locale.ROOT), key -> ResolvableProfile.createUnresolved((String)name));
        return FwmGfx.cached(profile, DefaultPlayerSkin.getDefaultSkin());
    }

    private static PlayerSkin skin() {
        UUID uuid = FwmGfx.playerUuid();
        if (uuid == null) {
            return DefaultPlayerSkin.getDefaultSkin();
        }
        ResolvableProfile profile = PROFILES.computeIfAbsent(uuid.toString(), key -> ResolvableProfile.createUnresolved((UUID)uuid));
        return FwmGfx.cached(profile, DefaultPlayerSkin.get((UUID)uuid));
    }

    private static PlayerSkin cached(ResolvableProfile profile, PlayerSkin fallback) {
        try {
            PlayerSkin textures = Minecraft.getInstance().playerSkinRenderCache().getOrDefault(profile).playerSkin();
            return textures == null ? fallback : textures;
        }
        catch (RuntimeException e) {
            return fallback;
        }
    }

    private static UUID playerUuid() {
        GameProfile profile = Minecraft.getInstance().getGameProfile();
        return profile == null ? null : profile.id();
    }

    public static String playerName() {
        GameProfile profile = Minecraft.getInstance().getGameProfile();
        String name = profile == null ? null : profile.name();
        return name == null ? "" : name;
    }
}

