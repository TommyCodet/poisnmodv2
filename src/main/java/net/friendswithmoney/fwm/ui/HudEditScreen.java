/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 */
package net.friendswithmoney.fwm.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.ui.FwmHud;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.FwmTutorial;
import net.friendswithmoney.fwm.ui.Lang;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public class HudEditScreen
extends Screen {
    private static final Map<String, int[]> FALLBACK = new LinkedHashMap<String, int[]>();
    private static final int HANDLE = 8;
    private final Screen parent;
    private final Map<String, int[]> boxes = new LinkedHashMap<String, int[]>();
    private final Map<String, int[]> unscaled = new LinkedHashMap<String, int[]>();
    private String dragging;
    private String resizing;
    private int corner = 3;
    private int grabX;
    private int grabY;

    public HudEditScreen(Screen parent) {
        super((Component)Lang.of("fwm.hudedit.title", new Object[0]));
        this.parent = parent;
    }

    protected void init() {
        FwmTutorial.undock();
        Map<String, int[]> live = FwmHud.bounds();
        for (String id : FALLBACK.keySet()) {
            int y;
            int h;
            if (!HudEditScreen.shownHere(id)) continue;
            int[] known = live.get(id);
            int[] size = FALLBACK.get(id);
            float scale = FwmHud.scaleOf(id);
            int w = known != null ? known[2] : Math.round((float)size[0] * scale);
            int n = h = known != null ? known[3] : Math.round((float)size[1] * scale);
            if ("secondchat".equals(id)) {
                int[] sample = FwmHud.sampleSize(this.font, 1.0f);
                this.unscaled.put(id, sample);
                w = Math.round((float)sample[0] * scale);
                h = Math.round((float)sample[1] * scale);
                known = null;
            } else {
                this.unscaled.put(id, new int[]{Math.max(8, Math.round((float)w / scale)), Math.max(8, Math.round((float)h / scale))});
            }
            FwmConfig.HudSpot spot = FwmHud.spot(id);
            int x = known != null ? known[0] : this.defaultX(id, w);
            int n2 = y = known != null ? known[1] : this.defaultY(id, h);
            if (spot.moved()) {
                x = Math.round(spot.x * (float)Math.max(0, this.width - w));
                y = Math.round(spot.y * (float)Math.max(0, this.height - h));
            }
            this.boxes.put(id, new int[]{x, y, w, h});
        }
    }

    private int defaultX(String id, int w) {
        return switch (id) {
            case "tool", "minimap", "todos", "donors" -> 6 + FwmTutorial.reservedLeft(this.width);
            case "priceping" -> (this.width - w) / 2;
            case "hearts" -> this.width / 2 + FwmHud.HEARTS_BOX[2];
            case "hotbar" -> this.width / 2 + FwmHud.HOTBAR_BOX[2];
            default -> this.width - 6 - w - FwmTutorial.reservedRight(this.width);
        };
    }

    private int defaultY(String id, int h) {
        return switch (id) {
            case "secondchat" -> this.height - 60 - h;
            case "toast" -> 64;
            case "envelope" -> 96;
            case "tpa" -> 128;
            case "minimap" -> 90;
            case "priceping" -> 24;
            case "money" -> 120;
            case "invvalue" -> 154;
            case "auction" -> 180;
            case "spawners" -> 40;
            case "todos" -> 60;
            case "donors" -> 142;
            case "hearts" -> this.height - FwmHud.HEARTS_BOX[3];
            case "hotbar" -> this.height - FwmHud.HOTBAR_BOX[3];
            default -> 6;
        };
    }

    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        super.extractRenderState(ctx, mouseX, mouseY, delta);
        for (Map.Entry<String, int[]> entry : this.boxes.entrySet()) {
            int[] box = entry.getValue();
            boolean hovered = mouseX >= box[0] && mouseX < box[0] + box[2] && mouseY >= box[1] && mouseY < box[1] + box[3];
            boolean active = entry.getKey().equals(this.dragging);
            boolean preview = "secondchat".equals(entry.getKey());
            FwmTheme.box(ctx, box[0], box[1], box[2], box[3], active ? FwmTheme.ACCENT_DIM : FwmTheme.RAISED, active || hovered ? FwmTheme.ACCENT : FwmTheme.OFF);
            MutableComponent label = Lang.of("fwm.hudedit." + entry.getKey(), new Object[0]);
            if (preview) {
                FwmHud.drawSample(ctx, this.font, box[0], box[1], FwmHud.scaleOf(entry.getKey()));
                ctx.text(this.font, (Component)label, box[0], box[1] - 10, active || hovered ? FwmTheme.ACCENT : FwmTheme.TEXT_DIM);
            } else {
                int n = box[0] + Math.max(3, (box[2] - this.font.width((FormattedText)label)) / 2);
                int n2 = box[1];
                int n3 = box[3];
                Objects.requireNonNull(this.font);
                ctx.text(this.font, (Component)label, n, n2 + Math.max(3, (n3 - 9) / 2), active || hovered ? FwmTheme.ACCENT : FwmTheme.TEXT);
            }
            if (!HudEditScreen.resizable(entry.getKey())) continue;
            int under = HudEditScreen.cornerAt(entry.getKey(), box, mouseX, mouseY);
            boolean pulling = entry.getKey().equals(this.resizing);
            if (HudEditScreen.growsLeft(entry.getKey())) {
                this.grip(ctx, HudEditScreen.handleX(entry.getKey(), box), box[1] + box[3] - 8, under >= 0 || pulling);
                continue;
            }
            for (int c = 0; c < 4; ++c) {
                int hx = c % 2 == 0 ? box[0] : box[0] + box[2] - 8;
                int hy = c < 2 ? box[1] : box[1] + box[3] - 8;
                this.grip(ctx, hx, hy, under == c || pulling && this.corner == c);
            }
        }
        if (!FwmTutorial.active()) {
            MutableComponent hint = Lang.of("fwm.hudedit.hint", new Object[0]);
            ctx.text(this.font, (Component)hint, (this.width - this.font.width((FormattedText)hint)) / 2, this.height - 35, FwmTheme.TEXT);
            MutableComponent hint2 = Lang.of("fwm.hudedit.hint2", new Object[0]);
            ctx.text(this.font, (Component)hint2, (this.width - this.font.width((FormattedText)hint2)) / 2, this.height - 24, FwmTheme.TEXT_DIM);
            MutableComponent reset = Lang.of("fwm.hudedit.reset", new Object[0]);
            ctx.text(this.font, (Component)reset, (this.width - this.font.width((FormattedText)reset)) / 2, this.height - 13, FwmTheme.TEXT_DIM);
        }
        FwmTutorial.render(ctx, this.font, this.width, this.height, mouseX, mouseY);
    }

    private void grip(GuiGraphicsExtractor ctx, int x, int y, boolean hot) {
        ctx.fill(x, y, x + 8, y + 8, hot ? FwmTheme.ACCENT : FwmTheme.ACCENT_DIM);
        FwmTheme.box(ctx, x, y, 8, 8, 0, hot ? FwmTheme.TEXT : FwmTheme.ACCENT);
    }

    private static boolean shownHere(String id) {
        String feature = switch (id) {
            case "macro" -> "fwm.tab.sellmacro";
            case "toast", "envelope" -> "fwm.tab.messages";
            case "tool" -> "fwm.tab.toolguard";
            case "secondchat" -> "fwm.tab.secondchat";
            case "tpa" -> "fwm.feature.tpawarn";
            case "minimap" -> "fwm.feature.rtpminimap";
            case "priceping" -> "fwm.feature.priceping";
            case "money" -> "fwm.tab.money";
            case "invvalue" -> "fwm.feature.invvalue";
            case "auction" -> "fwm.tab.auction";
            case "spawners" -> "fwm.tab.spawner";
            case "todos" -> "fwm.tab.todos";
            case "donors" -> "fwm.tab.donations";
            default -> null;
        };
        return feature == null || Features.allowedHere(feature);
    }

    private static boolean resizable(String id) {
        return !"macro".equals(id);
    }

    private static boolean growsLeft(String id) {
        return "secondchat".equals(id);
    }

    private static int handleX(String id, int[] box) {
        return HudEditScreen.growsLeft(id) ? box[0] : box[0] + box[2] - 8;
    }

    private static int cornerAt(String id, int[] box, double mouseX, double mouseY) {
        if (HudEditScreen.growsLeft(id)) {
            return HudEditScreen.overHandle(id, box, mouseX, mouseY) ? 2 : -1;
        }
        for (int c = 0; c < 4; ++c) {
            int hy;
            int hx = c % 2 == 0 ? box[0] : box[0] + box[2] - 8;
            int n = hy = c < 2 ? box[1] : box[1] + box[3] - 8;
            if (!(mouseX >= (double)hx) || !(mouseX < (double)(hx + 8)) || !(mouseY >= (double)hy) || !(mouseY < (double)(hy + 8))) continue;
            return c;
        }
        return -1;
    }

    private static boolean overHandle(String id, int[] box, double mouseX, double mouseY) {
        int hx = HudEditScreen.handleX(id, box);
        return mouseX >= (double)hx && mouseX < (double)(hx + 8) && mouseY >= (double)(box[1] + box[3] - 8) && mouseY < (double)(box[1] + box[3]);
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        int[] box;
        if (FwmTutorial.mouseClicked(click.x(), click.y(), click.button())) {
            return true;
        }
        if (FwmTutorial.consumes(click.x(), this.width)) {
            return true;
        }
        if (click.button() != 0) {
            return super.mouseClicked(click, doubled);
        }
        String hit = null;
        String handle = null;
        int handleCorner = 3;
        for (Map.Entry<String, int[]> entry : this.boxes.entrySet()) {
            int c;
            int[] box2 = entry.getValue();
            int n = c = HudEditScreen.resizable(entry.getKey()) ? HudEditScreen.cornerAt(entry.getKey(), box2, click.x(), click.y()) : -1;
            if (c >= 0) {
                handle = entry.getKey();
                handleCorner = c;
            }
            if (!(click.x() >= (double)box2[0]) || !(click.x() < (double)(box2[0] + box2[2])) || !(click.y() >= (double)box2[1]) || !(click.y() < (double)(box2[1] + box2[3]))) continue;
            hit = entry.getKey();
        }
        if (handle != null) {
            box = this.boxes.get(handle);
            this.resizing = handle;
            this.corner = handleCorner;
            boolean left = HudEditScreen.growsLeft(handle) || this.corner % 2 == 0;
            boolean top = !HudEditScreen.growsLeft(handle) && this.corner < 2;
            this.grabX = (int)click.x() - (left ? box[0] : box[0] + box[2]);
            this.grabY = (int)click.y() - (top ? box[1] : box[1] + box[3]);
            return true;
        }
        if (hit == null) {
            return super.mouseClicked(click, doubled);
        }
        box = this.boxes.get(hit);
        this.dragging = hit;
        this.grabX = (int)click.x() - box[0];
        this.grabY = (int)click.y() - box[1];
        return true;
    }

    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (this.resizing != null) {
            this.resize(click.x(), click.y());
            return true;
        }
        if (this.dragging == null) {
            return super.mouseDragged(click, deltaX, deltaY);
        }
        int[] box = this.boxes.get(this.dragging);
        box[0] = HudEditScreen.clamp((int)click.x() - this.grabX, this.width - box[2]);
        box[1] = HudEditScreen.clamp((int)click.y() - this.grabY, this.height - box[3]);
        return true;
    }

    private void resize(double mouseX, double mouseY) {
        int[] box = this.boxes.get(this.resizing);
        int[] size = this.unscaled.get(this.resizing);
        FwmConfig.HudSpot spot = FwmHud.spot(this.resizing);
        if ("secondchat".equals(this.resizing)) {
            this.resizeChat(box, size, spot, mouseX, mouseY);
            return;
        }
        boolean left = this.corner % 2 == 0;
        boolean top = this.corner < 2;
        int anchorX = left ? box[0] + box[2] : box[0];
        int anchorY = top ? box[1] + box[3] : box[1];
        double edge = mouseX - (double)this.grabX;
        double wanted = left ? (double)anchorX - edge : edge - (double)anchorX;
        spot.resize((float)wanted / (float)size[0]);
        box[2] = Math.round((float)size[0] * spot.scale());
        box[3] = Math.round((float)size[1] * spot.scale());
        box[0] = left ? anchorX - box[2] : anchorX;
        box[1] = top ? anchorY - box[3] : anchorY;
    }

    private void resizeChat(int[] box, int[] size, FwmConfig.HudSpot spot, double mouseX, double mouseY) {
        spot.resize((float)(mouseY - (double)this.grabY - (double)box[1]) / (float)Math.max(1, size[1]));
        int right = box[0] + box[2];
        int wanted = Math.round(((float)right - (float)(mouseX - (double)this.grabX)) / spot.scale());
        FwmConfig.INSTANCE.secondChatWidth = Math.max(100, Math.min(420, wanted));
        this.unscaled.put("secondchat", FwmHud.sampleSize(this.font, 1.0f));
        int[] drawn = FwmHud.sampleSize(this.font, spot.scale());
        box[0] = right - drawn[0];
        box[2] = drawn[0];
        box[3] = drawn[1];
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        if (this.resizing != null) {
            if (!HudEditScreen.growsLeft(this.resizing)) {
                this.store(this.resizing);
            }
            this.resizing = null;
            ConfigManager.save();
        }
        if (this.dragging != null) {
            this.store(this.dragging);
            this.dragging = null;
            ConfigManager.save();
        }
        return super.mouseReleased(click);
    }

    private void store(String id) {
        int[] box = this.boxes.get(id);
        float fx = this.width - box[2] <= 0 ? 0.0f : (float)box[0] / (float)(this.width - box[2]);
        float fy = this.height - box[3] <= 0 ? 0.0f : (float)box[1] / (float)(this.height - box[3]);
        FwmHud.spot(id).set(fx, fy);
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(Math.max(0, max), value));
    }

    public boolean keyPressed(KeyEvent input) {
        if (input.key() != 82 && FwmTutorial.keyPressed(input.key())) {
            return true;
        }
        if (input.key() == 82) {
            for (String id : this.boxes.keySet()) {
                FwmHud.spot(id).reset();
            }
            ConfigManager.save();
            this.init();
            return true;
        }
        return super.keyPressed(input);
    }

    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }

    static {
        FALLBACK.put("macro", new int[]{150, 40});
        FALLBACK.put("toast", new int[]{150, 26});
        FALLBACK.put("envelope", new int[]{40, 16});
        FALLBACK.put("tpa", new int[]{128, 41});
        FALLBACK.put("tool", new int[]{200, 33});
        FALLBACK.put("secondchat", new int[]{240, 50});
        FALLBACK.put("minimap", new int[]{128, 150});
        FALLBACK.put("priceping", new int[]{170, 41});
        FALLBACK.put("money", new int[]{150, 30});
        FALLBACK.put("invvalue", new int[]{90, 19});
        FALLBACK.put("auction", new int[]{160, 41});
        FALLBACK.put("spawners", new int[]{150, 100});
        FALLBACK.put("todos", new int[]{130, 50});
        FALLBACK.put("donors", new int[]{130, 56});
        FALLBACK.put("hearts", new int[]{FwmHud.HEARTS_BOX[0], FwmHud.HEARTS_BOX[1]});
        FALLBACK.put("hotbar", new int[]{FwmHud.HOTBAR_BOX[0], FwmHud.HOTBAR_BOX[1]});
    }
}

