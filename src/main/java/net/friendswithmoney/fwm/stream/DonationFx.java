/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 *  org.joml.Matrix3x2fStack
 */
package net.friendswithmoney.fwm.stream;

import net.friendswithmoney.fwm.stream.DonationSounds;
import net.friendswithmoney.fwm.stream.DonationStore;
import net.friendswithmoney.fwm.stream.Donations;
import net.friendswithmoney.fwm.stream.fx.FxAnim;
import net.friendswithmoney.fwm.stream.fx.FxCanvas;
import net.friendswithmoney.fwm.stream.fx.FxPlayer;
import net.friendswithmoney.fwm.ui.panel.DonationStudioScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

public final class DonationFx {
    private static final FxPlayer LIVE = new FxPlayer();
    private static final FxCanvas CANVAS = new FxCanvas();

    private DonationFx() {
    }

    public static boolean playing() {
        return LIVE.running();
    }

    public static String current() {
        return LIVE.running() && DonationFx.LIVE.play.anim != null ? DonationFx.LIVE.play.anim.id + ":" + DonationFx.LIVE.play.donor : null;
    }

    static boolean start(Donations.Item item, String animId) {
        FxAnim anim = FxAnim.byId(animId);
        if ("chat".equals(anim.id)) {
            DonationFx.chatLine(item);
            return false;
        }
        DonationFx.LIVE.loopPause = -1;
        LIVE.load(item.name, item.amount, item.total, item.count, item.tier, anim);
        DonationFx.LIVE.play.preview = false;
        DonationFx.LIVE.play.sound = true;
        DonationFx.LIVE.play.test = item.test;
        LIVE.start(System.currentTimeMillis());
        return true;
    }

    static void chatLine(Donations.Item item) {
        Minecraft client = Minecraft.getInstance();
        if (client.gui == null) {
            return;
        }
        LIVE.load(item.name, item.amount, item.total, item.count, item.tier, FxAnim.byId("chat"));
        String line = DonationFx.LIVE.play.line;
        client.gui.hud.getChat().addClientSystemMessage((Component)Component.literal((String)"[FWM] ").withStyle(s -> s.withColor(item.tier.color & 0xFFFFFF)).append((Component)Component.literal((String)line).withStyle(ChatFormatting.WHITE)));
        DonationSounds.play(item.tier.sound, item.tier.pitch, item.tier.volume);
    }

    public static void skip() {
        if (!LIVE.running()) {
            return;
        }
        LIVE.stop();
        DonationSounds.stopAll();
    }

    static void tick(long now) {
        if (!LIVE.running()) {
            return;
        }
        if (!LIVE.update(now)) {
            return;
        }
        LIVE.sounds(now);
    }

    static void renderHud(GuiGraphicsExtractor ctx) {
        if (Minecraft.getInstance().gui.screen() != null) {
            return;
        }
        DonationStudioScreen.gameFrame();
        if (!LIVE.running()) {
            return;
        }
        FxCanvas.newRootLayer(ctx);
        DonationFx.draw(ctx);
    }

    static void renderOverScreen(GuiGraphicsExtractor ctx) {
        if (!LIVE.running()) {
            return;
        }
        FxCanvas.newRootLayer(ctx);
        DonationFx.draw(ctx);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void draw(GuiGraphicsExtractor ctx) {
        long now = System.currentTimeMillis();
        if (!LIVE.update(now)) {
            return;
        }
        FxCanvas c = CANVAS;
        c.shake = DonationStore.get().shake;
        Matrix3x2fStack mat = ctx.pose();
        float sx = Math.abs(mat.m00()) < 0.01f ? 1.0f : mat.m00();
        float sy = Math.abs(mat.m11()) < 0.01f ? 1.0f : mat.m11();
        float x0 = -mat.m20() / sx;
        float y0 = -mat.m21() / sy;
        c.begin(ctx, x0, y0, (float)ctx.guiWidth() / sx, (float)ctx.guiHeight() / sy, false);
        try {
            LIVE.draw(c, now);
        }
        finally {
            c.end();
        }
    }
}

