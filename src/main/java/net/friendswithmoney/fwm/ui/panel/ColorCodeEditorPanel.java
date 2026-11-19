/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.input.MouseButtonInfo
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.friendswithmoney.fwm.colorcode.CodeStyle;
import net.friendswithmoney.fwm.colorcode.ColorCode;
import net.friendswithmoney.fwm.colorcode.Gradient;
import net.friendswithmoney.fwm.mixin.TextFieldWidgetAccessor;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.ColorCodePanel;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.widget.ColorRow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ColorCodeEditorPanel
extends Panel {
    private static final int BTN_H = 13;
    private static final int NAME_Y = 17;
    private static final int NAME_H = 16;
    private static final int COMMAND_Y = 37;
    private static final int STATUS_Y = 49;
    private static final int TOP_H = 62;
    private static final int HEAD_H = 11;
    private static final int SCROLL_W = 4;
    private static final int GAP = 4;
    private static final int SCROLL_STEP = 14;
    private static final int PREVIEW_Y = 0;
    private static final int PREVIEW_H = 22;
    private static final int COLORS_Y = 37;
    private static final int SWATCH_H = 11;
    private static final int FORMATS_Y = 77;
    private static final int GRADIENT_Y = 123;
    private static final int PRESET_COLS = 4;
    private static final int HEX_Y = 195;
    private static final int IMPORT_Y = 317;
    private static final int CONTENT_H = 346;
    private static final int HEX_FIELD_W = 52;
    private static final int RED = -43691;
    private static final int GOLD = -22016;
    private static final long COPIED_MS = 1600L;
    private static final long FLASH_MS = 900L;
    private static final long DOUBLE_CLICK_MS = 250L;
    private static final long RESULT_MS = 5000L;
    private static final CodeStyle.Format[] FORMATS = CodeStyle.Format.values();
    private static final String[] FORMAT_KEYS = new String[]{"fwm.colorcodes.bold", "fwm.colorcodes.underline", "fwm.colorcodes.italic", "fwm.colorcodes.strike", "fwm.colorcodes.obf", "fwm.colorcodes.reset"};
    private final PanelHost host;
    private final ColorCodePanel.User user;
    private EditBox name;
    private EditBox hex;
    private EditBox importField;
    private String hexText = "FF3DAF";
    private String importText = "";
    private final List<ColorRow> hsvRows = new ArrayList<ColorRow>();
    private double hue;
    private double sat;
    private double val;
    private boolean hexError;
    private boolean syncing;
    private int seenVersion = -1;
    private int scroll;
    private long copiedAt;
    private long flashAt;
    private Component gradientResult;
    private boolean gradientResultBad;
    private long gradientAt;
    private EditBox dragging;
    private ColorRow draggingRow;
    private EditBox lastClicked;
    private long lastClickAt;

    public ColorCodeEditorPanel(PanelHost host, ColorCodePanel.User user) {
        this.host = host;
        this.user = user;
        this.setHsvFrom(Integer.parseInt(this.hexText, 16));
    }

    @Override
    public Component title() {
        return Lang.of("fwm.colorcodes.editor", new Object[0]);
    }

    @Override
    protected void onResize() {
        this.detachFields();
        Font font = Minecraft.getInstance().font;
        this.name = new EditBox(font, 0, 0, Math.max(20, this.width - 8), 10, (Component)Lang.of("fwm.tab.colorcodes", new Object[0]));
        this.name.setBordered(false);
        this.name.setMaxLength(64);
        this.name.setTextColor(-1);
        this.name.setHint((Component)Lang.of("fwm.colorcodes.placeholder", new Object[0]));
        this.name.addFormatter(this::formatName);
        this.name.setResponder(text -> {
            if (!this.syncing) {
                this.user.code.reconcile((String)text);
            }
        });
        this.hex = new EditBox(font, 0, 0, 44, 10, (Component)Lang.of("fwm.colorcodes.hex", new Object[0]));
        this.hex.setBordered(false);
        this.hex.setMaxLength(7);
        this.hex.setTextColor(FwmTheme.TEXT);
        this.hex.setValue(this.hexText);
        this.hex.setResponder(text -> {
            StringBuilder cleaned = new StringBuilder();
            int digits = 0;
            for (int i = 0; i < text.length(); ++i) {
                char c = text.charAt(i);
                if (c == '#' && cleaned.length() == 0) {
                    cleaned.append(c);
                    continue;
                }
                if (Character.digit(c, 16) < 0 || digits >= 6) continue;
                cleaned.append(c);
                ++digits;
            }
            if (!cleaned.toString().equals(text)) {
                this.hex.setValue(cleaned.toString());
                return;
            }
            this.hexText = text;
            if (this.syncing) {
                return;
            }
            String normalized = ColorCode.normalizeHex(text);
            if (normalized != null) {
                this.setHsvFrom(Integer.parseInt(normalized, 16));
                this.hexError = false;
            }
        });
        this.importField = new EditBox(font, 0, 0, 60, 10, (Component)Lang.of("fwm.colorcodes.import", new Object[0]));
        this.importField.setBordered(false);
        this.importField.setMaxLength(256);
        this.importField.setTextColor(FwmTheme.TEXT);
        this.importField.setHint((Component)Lang.of("fwm.colorcodes.importPlaceholder", new Object[0]));
        this.importField.setValue(this.importText);
        this.importField.setResponder(text -> {
            this.importText = text;
        });
        this.hsvRows.clear();
        this.hsvRows.add(new ColorRow((Component)Lang.of("fwm.colorcodes.hue", new Object[0]), () -> this.hue, v -> {
            this.hue = v;
            this.pushHex();
        }, t -> ColorCodeEditorPanel.hsv(t, 1.0, 1.0), null, null, null));
        this.hsvRows.add(new ColorRow((Component)Lang.of("fwm.colorcodes.saturation", new Object[0]), () -> this.sat, v -> {
            this.sat = v;
            this.pushHex();
        }, t -> ColorCodeEditorPanel.hsv(this.hue, t, this.val), null, null, null));
        this.hsvRows.add(new ColorRow((Component)Lang.of("fwm.colorcodes.brightness", new Object[0]), () -> this.val, v -> {
            this.val = v;
            this.pushHex();
        }, t -> ColorCodeEditorPanel.hsv(this.hue, this.sat, t), null, null, null));
        this.seenVersion = -1;
        this.syncFromSession();
        this.host.attach((AbstractWidget)this.name);
        this.host.attach((AbstractWidget)this.hex);
        this.host.attach((AbstractWidget)this.importField);
        this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll));
        this.position();
    }

    private void detachFields() {
        if (this.name != null) {
            this.host.detach((AbstractWidget)this.name);
        }
        if (this.hex != null) {
            this.host.detach((AbstractWidget)this.hex);
        }
        if (this.importField != null) {
            this.host.detach((AbstractWidget)this.importField);
        }
    }

    @Override
    public void onHidden() {
        this.detachFields();
        this.dragging = null;
        this.draggingRow = null;
    }

    private void syncFromSession() {
        if (this.user.version == this.seenVersion) {
            return;
        }
        this.seenVersion = this.user.version;
        this.syncing = true;
        this.name.setValue(this.user.code.text());
        this.name.moveCursorToEnd(false);
        this.syncing = false;
        if (!this.name.getValue().equals(this.user.code.text())) {
            this.user.code.reconcile(this.name.getValue());
        }
    }

    private FormattedCharSequence formatName(String visible, int first) {
        List<CodeStyle> styles = this.user.code.styles();
        ArrayList<FormattedCharSequence> parts = new ArrayList<FormattedCharSequence>(visible.length());
        for (int i = 0; i < visible.length(); ++i) {
            int index = first + i;
            CodeStyle style = index < styles.size() ? styles.get(index) : CodeStyle.DEFAULT;
            parts.add(FormattedCharSequence.forward((String)String.valueOf(visible.charAt(i)), (Style)ColorCodePanel.style(style, false)));
        }
        return FormattedCharSequence.composite(parts);
    }

    private int selectionStart() {
        return this.clampToText(Math.min(this.name.getCursorPosition(), ((TextFieldWidgetAccessor)this.name).getFwmSelectionEnd()));
    }

    private int selectionEnd() {
        return this.clampToText(Math.max(this.name.getCursorPosition(), ((TextFieldWidgetAccessor)this.name).getFwmSelectionEnd()));
    }

    private int clampToText(int index) {
        return Math.max(0, Math.min(this.user.code.text().length(), index));
    }

    private boolean hasSelection() {
        return this.selectionStart() < this.selectionEnd();
    }

    private void onSelection(BiConsumer<Integer, Integer> edit) {
        if (!this.hasSelection()) {
            this.flashAt = System.currentTimeMillis();
            return;
        }
        edit.accept(this.selectionStart(), this.selectionEnd());
        this.focus(this.name);
    }

    private void focus(EditBox field) {
        this.name.setFocused(field == this.name);
        this.hex.setFocused(field == this.hex);
        this.importField.setFocused(field == this.importField);
    }

    private static int hsv(double h, double s, double v) {
        double g;
        double r;
        double hh = (h % 1.0 + 1.0) % 1.0 * 6.0;
        int sector = (int)Math.floor(hh) % 6;
        double f = hh - Math.floor(hh);
        double p = v * (1.0 - s);
        double q = v * (1.0 - f * s);
        double t = v * (1.0 - (1.0 - f) * s);
        return (int)Math.round(r * 255.0) << 16 | (int)Math.round(g * 255.0) << 8 | (int)Math.round((switch (sector) {
            case 0 -> {
                r = v;
                g = t;
                yield p;
            }
            case 1 -> {
                r = q;
                g = v;
                yield p;
            }
            case 2 -> {
                r = p;
                g = v;
                yield t;
            }
            case 3 -> {
                r = p;
                g = q;
                yield v;
            }
            case 4 -> {
                r = t;
                g = p;
                yield v;
            }
            default -> {
                r = v;
                g = p;
                yield q;
            }
        }) * 255.0);
    }

    private void setHsvFrom(int rgb) {
        double r = (double)(rgb >> 16 & 0xFF) / 255.0;
        double g = (double)(rgb >> 8 & 0xFF) / 255.0;
        double b = (double)(rgb & 0xFF) / 255.0;
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double d = max - min;
        this.val = max;
        double d2 = this.sat = max == 0.0 ? 0.0 : d / max;
        if (d == 0.0) {
            return;
        }
        double h = max == r ? (g - b) / d % 6.0 : (max == g ? (b - r) / d + 2.0 : (r - g) / d + 4.0);
        this.hue = (h / 6.0 + 1.0) % 1.0;
    }

    private void pushHex() {
        this.syncing = true;
        this.hex.setValue(String.format("%06X", ColorCodeEditorPanel.hsv(this.hue, this.sat, this.val)));
        this.syncing = false;
        this.hexError = false;
    }

    private int currentHexRgb() {
        String normalized = ColorCode.normalizeHex(this.hexText);
        return normalized != null ? Integer.parseInt(normalized, 16) : ColorCodeEditorPanel.hsv(this.hue, this.sat, this.val);
    }

    private void applyHex() {
        String normalized = ColorCode.normalizeHex(this.hexText);
        if (normalized == null) {
            this.hexError = true;
            return;
        }
        this.hexError = false;
        this.onSelection((s, e) -> this.user.code.applyHex((int)s, (int)e, normalized));
    }

    private void applyGradient(int[] stops) {
        ColorCode code = this.user.code;
        if (code.text().isEmpty()) {
            this.flashAt = System.currentTimeMillis();
            return;
        }
        int start = this.hasSelection() ? this.selectionStart() : 0;
        int end = this.hasSelection() ? this.selectionEnd() : code.text().length();
        Gradient.Result result = Gradient.apply(code, start, end, stops, this.user.mode);
        String kind = switch (result.kind()) {
            default -> throw new MatchException(null, null);
            case Gradient.Kind.HEX -> Lang.str("fwm.colorcodes.gradientHex", new Object[0]);
            case Gradient.Kind.STANDARD -> Lang.str("fwm.colorcodes.gradientStandard", new Object[0]);
            case Gradient.Kind.MIXED -> Lang.str("fwm.colorcodes.gradientMixed", new Object[0]);
        };
        this.gradientResultBad = !result.fits();
        this.gradientResult = result.fits() ? Lang.of("fwm.colorcodes.gradientDone", result.steps(), kind) : Lang.of("fwm.colorcodes.gradientTooLong", new Object[0]);
        this.gradientAt = System.currentTimeMillis();
        this.focus(this.name);
    }

    private int bodyTop() {
        return this.y + 62;
    }

    private int bodyHeight() {
        return Math.max(0, this.y + this.height - this.bodyTop());
    }

    private int maxScroll() {
        return Math.max(0, 346 - this.bodyHeight());
    }

    private int contentW() {
        return this.width - 4 - 2;
    }

    private int at(int offset) {
        return this.bodyTop() - this.scroll + offset;
    }

    private boolean inBody(double my) {
        return my >= (double)this.bodyTop() && my < (double)(this.y + this.height);
    }

    private int copyW(Font font) {
        return Math.max(font.width((FormattedText)Lang.of("fwm.colorcodes.copy", new Object[0])), font.width((FormattedText)Lang.of("fwm.colorcodes.copied", new Object[0]))) + 12;
    }

    private int modeW(Font font, ColorCode.Mode mode) {
        return font.width((FormattedText)Lang.of(ColorCodeEditorPanel.modeKey(mode), new Object[0])) + 12;
    }

    private static String modeKey(ColorCode.Mode mode) {
        return mode == ColorCode.Mode.SIGN ? "fwm.colorcodes.sign" : "fwm.colorcodes.rename";
    }

    private int swatchW() {
        return this.contentW() / 8;
    }

    private int formatW() {
        return (this.contentW() - 8) / 3;
    }

    private int presetW() {
        return (this.contentW() - 12) / 4;
    }

    private int presetX(int i) {
        return this.x + i % 4 * (this.presetW() + 4);
    }

    private int presetY(int i) {
        return this.at(123) + 11 + i / 4 * 16;
    }

    private int gradientRowY() {
        return this.at(123) + 11 + 32;
    }

    private int gradientApplyW(Font font) {
        return font.width((FormattedText)Lang.of("fwm.colorcodes.gradientApply", new Object[0])) + 12;
    }

    private int gradientApplyX(Font font) {
        return this.x + this.contentW() - this.gradientApplyW(font);
    }

    private int gradientToX(Font font) {
        return this.gradientApplyX(font) - 4 - 13;
    }

    private int hexRowY() {
        return this.at(195) + 11;
    }

    private int applyX() {
        return this.x + 17 + 52 + 4;
    }

    private int loadW(Font font) {
        return font.width((FormattedText)Lang.of("fwm.colorcodes.importLoad", new Object[0])) + 12;
    }

    private void position() {
        Font font = Minecraft.getInstance().font;
        this.name.setX(this.x + 4);
        this.name.setY(this.y + 17 + 4);
        this.hex.setX(this.x + 17 + 4);
        this.hex.setY(this.hexRowY() + 2);
        int loadW = this.loadW(font);
        this.importField.setX(this.x + 4);
        this.importField.setY(this.at(317) + 11 + 2);
        this.importField.setWidth(Math.max(20, this.contentW() - loadW - 4 - 8));
        int rowsTop = this.hexRowY() + 13 + 2 + 11;
        for (int i = 0; i < this.hsvRows.size(); ++i) {
            this.hsvRows.get(i).setBounds(this.x, rowsTop + i * 26, this.contentW());
        }
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        font = Minecraft.getInstance().font;
        if (this.name == null) {
            return;
        }
        this.syncFromSession();
        this.position();
        this.renderTop(ctx, font, mouseX, mouseY, delta);
        ctx.enableScissor(this.x, this.bodyTop(), this.x + this.width, this.y + this.height);
        this.renderPreview(ctx, font);
        this.renderColors(ctx, font, mouseX, mouseY);
        this.renderFormats(ctx, font, mouseX, mouseY);
        this.renderGradient(ctx, font, mouseX, mouseY);
        this.renderHex(ctx, font, mouseX, mouseY, delta);
        this.renderImport(ctx, font, mouseX, mouseY, delta);
        ctx.disableScissor();
        this.renderScrollbar(ctx);
        if (this.inBody(mouseY)) {
            this.renderHoverTip(ctx, font, mouseX, mouseY);
        }
    }

    private void renderTop(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        int color;
        MutableComponent status;
        boolean flashing;
        ColorCode code = this.user.code;
        ColorCode.Mode mode = this.user.mode;
        int length = code.length(mode);
        boolean over = code.tooLong(mode);
        int mx = this.x;
        for (ColorCode.Mode m : ColorCode.Mode.values()) {
            int w = this.modeW(font, m);
            ColorCodePanel.button(ctx, font, mx, this.y, w, 13, (Component)Lang.of(ColorCodeEditorPanel.modeKey(m), new Object[0]), ColorCodePanel.over(mouseX, mouseY, mx, this.y, w, 13), m == mode, true);
            mx += w + 2;
        }
        int cw = this.copyW(font);
        int cx = this.x + this.width - cw;
        boolean copied = System.currentTimeMillis() - this.copiedAt < 1600L;
        boolean canCopy = !over && !code.text().isEmpty();
        ColorCodePanel.button(ctx, font, cx, this.y, cw, 13, (Component)Lang.of(copied ? "fwm.colorcodes.copied" : "fwm.colorcodes.copy", new Object[0]), canCopy && ColorCodePanel.over(mouseX, mouseY, cx, this.y, cw, 13), copied, canCopy);
        String counter = Lang.str(mode.codesCount ? "fwm.colorcodes.counter" : "fwm.colorcodes.counterLetters", length, mode.limit);
        int counterColor = over ? -43691 : ((double)length >= (double)mode.limit * 0.85 ? -22016 : FwmTheme.TEXT_DIM);
        int n = cx - 5 - font.width(counter);
        Objects.requireNonNull(font);
        ctx.text(font, counter, n, this.y + (13 - 9) / 2 + 1, counterColor, false);
        FwmTheme.box(ctx, this.x, this.y + 17, this.width, 16, FwmTheme.INSET, this.name.isFocused() ? FwmTheme.ACCENT : FwmTheme.OFF);
        this.name.extractWidgetRenderState(ctx, mouseX, mouseY, delta);
        if (!this.name.isFocused() && this.hasSelection()) {
            int from = Math.max(this.x + 4, this.name.getScreenX(this.selectionStart()));
            int to = Math.min(this.x + this.width - 4, this.name.getScreenX(this.selectionEnd()));
            if (to > from) {
                ctx.fill(from, this.y + 17 + 16 - 3, to, this.y + 17 + 16 - 2, FwmTheme.ACCENT);
            }
        }
        MutableComponent command = Component.literal((String)(mode.command + " ")).withColor(FwmTheme.TEXT_DIM).append((Component)Component.literal((String)code.serialize()).withColor(over ? -43691 : FwmTheme.TEXT));
        ctx.text(font, ColorCodePanel.trimmed(font, (Component)command, this.width), this.x, this.y + 37, FwmTheme.TEXT, false);
        boolean bl = flashing = System.currentTimeMillis() - this.flashAt < 900L;
        if (over && length <= mode.limit) {
            status = Lang.of("fwm.colorcodes.tooLongChat", code.command(mode).length(), 256);
            color = -43691;
        } else if (over) {
            status = Lang.of(mode.codesCount ? "fwm.colorcodes.tooLong" : "fwm.colorcodes.tooLongLetters", mode.limit);
            color = -43691;
        } else if (!this.hasSelection()) {
            status = Lang.of("fwm.colorcodes.hint", new Object[0]);
            color = flashing ? -22016 : FwmTheme.TEXT_DIM;
        } else {
            status = Lang.of("fwm.colorcodes.selected", this.selectionEnd() - this.selectionStart());
            color = FwmTheme.TEXT_DIM;
        }
        ctx.text(font, ColorCodePanel.trimmed(font, (Component)status, this.width), this.x, this.y + 49, color, false);
        FwmTheme.divider(ctx, this.x, this.bodyTop() - 2, this.width);
    }

    private void heading(GuiGraphicsExtractor ctx, Font font, String key, int top) {
        ctx.text(font, (Component)Lang.of(key, new Object[0]), this.x, top + 1, FwmTheme.ACCENT, false);
    }

    private void renderPreview(GuiGraphicsExtractor ctx, Font font) {
        int top = this.at(0);
        this.heading(ctx, font, "fwm.colorcodes.preview", top);
        int by = top + 11;
        ItemStack icon = ColorCodeEditorPanel.previewIcon(this.user.mode == ColorCode.Mode.SIGN ? Items.OAK_SIGN : Items.NAME_TAG);
        ctx.item(icon, this.x + 2, by + 3);
        MutableComponent shown = this.user.code.text().isEmpty() ? Component.literal((String)"-").withColor(FwmTheme.TEXT_DIM) : ColorCodePanel.text(this.user.code);
        int room = this.contentW() - 24;
        int boxW = Math.min(room, font.width((FormattedText)shown) + 8);
        int bx = this.x + 22;
        int boxH = 16;
        int boxY = by + (22 - boxH) / 2;
        ColorCodePanel.tooltipBox(ctx, bx, boxY, boxW, boxH);
        ctx.text(font, ColorCodePanel.trimmed(font, (Component)shown, boxW - 8), bx + 4, boxY + 4, -1, true);
    }

    private void renderColors(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int top = this.at(37);
        this.heading(ctx, font, "fwm.colorcodes.colors", top);
        int sw = this.swatchW();
        for (int i = 0; i < 16; ++i) {
            int sx = this.x + i % 8 * sw;
            int sy = top + 11 + i / 8 * 13;
            boolean hovered = this.inBody(mouseY) && ColorCodePanel.over(mouseX, mouseY, sx, sy, sw - 2, 11);
            FwmTheme.box(ctx, sx, sy, sw - 2, 11, 0xFF000000 | ColorCode.RGB[i], hovered ? FwmTheme.TEXT : FwmTheme.OFF);
        }
    }

    private void renderFormats(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int top = this.at(77);
        this.heading(ctx, font, "fwm.colorcodes.formats", top);
        int bw = this.formatW();
        boolean marked = this.hasSelection();
        for (int i = 0; i < FORMAT_KEYS.length; ++i) {
            int bx = this.x + i % 3 * (bw + 4);
            int by = top + 11 + i / 3 * 16;
            MutableComponent label = Lang.of(FORMAT_KEYS[i], new Object[0]);
            boolean active = false;
            if (i < FORMATS.length) {
                CodeStyle.Format format = FORMATS[i];
                label = label.setStyle(Style.EMPTY.withBold(Boolean.valueOf(format == CodeStyle.Format.BOLD)).withUnderlined(Boolean.valueOf(format == CodeStyle.Format.UNDERLINE)).withItalic(Boolean.valueOf(format == CodeStyle.Format.ITALIC)).withStrikethrough(Boolean.valueOf(format == CodeStyle.Format.STRIKE)));
                active = marked && this.user.code.allHave(this.selectionStart(), this.selectionEnd(), format);
            }
            ColorCodePanel.button(ctx, font, bx, by, bw, 13, (Component)label, this.inBody(mouseY) && ColorCodePanel.over(mouseX, mouseY, bx, by, bw, 13), active, true);
        }
    }

    private static void gradientBar(GuiGraphicsExtractor ctx, int bx, int by, int bw, int bh, int[] stops, int edge) {
        FwmTheme.box(ctx, bx, by, bw, bh, -16777216, edge);
        int inner = bw - 2;
        for (int i = 0; i < inner; ++i) {
            int rgb = Gradient.at(stops, inner <= 1 ? 0.0 : (double)i / (double)(inner - 1));
            ctx.fill(bx + 1 + i, by + 1, bx + 2 + i, by + bh - 1, 0xFF000000 | rgb);
        }
    }

    private int[] ownStops() {
        return new int[]{this.user.gradientFrom, this.user.gradientTo};
    }

    private void renderGradient(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        boolean recent;
        this.heading(ctx, font, "fwm.colorcodes.gradient", this.at(123));
        int pw = this.presetW();
        for (int i = 0; i < Gradient.PRESETS.size(); ++i) {
            Gradient.Preset preset = Gradient.PRESETS.get(i);
            int px = this.presetX(i);
            int py = this.presetY(i);
            boolean hovered = this.inBody(mouseY) && ColorCodePanel.over(mouseX, mouseY, px, py, pw, 13);
            ColorCodeEditorPanel.gradientBar(ctx, px, py, pw, 13, preset.stops(), hovered ? FwmTheme.TEXT : FwmTheme.OFF);
            FormattedCharSequence label = ColorCodePanel.trimmed(font, (Component)Component.literal((String)Lang.t(preset.de(), preset.en())), pw - 4);
            int n = px + (pw - font.width(label)) / 2;
            Objects.requireNonNull(font);
            ctx.text(font, label, n, py + (13 - 9) / 2 + 1, -1, true);
        }
        int ry = this.gradientRowY();
        int toX = this.gradientToX(font);
        boolean overFrom = this.inBody(mouseY) && ColorCodePanel.over(mouseX, mouseY, this.x, ry, 13, 13);
        boolean overTo = this.inBody(mouseY) && ColorCodePanel.over(mouseX, mouseY, toX, ry, 13, 13);
        boolean overBar = this.inBody(mouseY) && ColorCodePanel.over(mouseX, mouseY, this.x + 15, ry, toX - this.x - 17, 13);
        FwmTheme.box(ctx, this.x, ry, 13, 13, 0xFF000000 | this.user.gradientFrom, overFrom ? FwmTheme.TEXT : FwmTheme.OFF);
        ColorCodeEditorPanel.gradientBar(ctx, this.x + 15, ry, toX - this.x - 17, 13, this.ownStops(), overBar ? FwmTheme.TEXT : FwmTheme.OFF);
        FwmTheme.box(ctx, toX, ry, 13, 13, 0xFF000000 | this.user.gradientTo, overTo ? FwmTheme.TEXT : FwmTheme.OFF);
        int ax = this.gradientApplyX(font);
        int aw = this.gradientApplyW(font);
        ColorCodePanel.button(ctx, font, ax, ry, aw, 13, (Component)Lang.of("fwm.colorcodes.gradientApply", new Object[0]), this.inBody(mouseY) && ColorCodePanel.over(mouseX, mouseY, ax, ry, aw, 13), false, true);
        boolean bl = recent = this.gradientResult != null && System.currentTimeMillis() - this.gradientAt < 5000L;
        Component info = recent ? this.gradientResult : Lang.of(this.hasSelection() ? "fwm.colorcodes.gradientSelection" : "fwm.colorcodes.gradientWhole", new Object[0]);
        int color = recent && this.gradientResultBad ? -43691 : FwmTheme.TEXT_DIM;
        ctx.text(font, ColorCodePanel.trimmed(font, info, this.contentW()), this.x, ry + 13 + 3, color, false);
    }

    private void renderHex(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        int color;
        MutableComponent info;
        this.heading(ctx, font, "fwm.colorcodes.hex", this.at(195));
        int ry = this.hexRowY();
        int rgb = this.currentHexRgb();
        FwmTheme.box(ctx, this.x, ry, 13, 13, 0xFF000000 | rgb, FwmTheme.OFF);
        FwmTheme.box(ctx, this.x + 17, ry, 52, 13, FwmTheme.INSET, this.hexError ? -43691 : (this.hex.isFocused() ? FwmTheme.ACCENT : FwmTheme.OFF));
        this.hex.extractWidgetRenderState(ctx, mouseX, mouseY, delta);
        int ax = this.applyX();
        int aw = this.x + this.contentW() - ax;
        ColorCodePanel.button(ctx, font, ax, ry, aw, 13, (Component)Lang.of("fwm.colorcodes.hexApply", new Object[0]), this.inBody(mouseY) && ColorCodePanel.over(mouseX, mouseY, ax, ry, aw, 13), false, true);
        if (this.hexError) {
            info = Lang.of("fwm.colorcodes.hexError", new Object[0]);
            color = -43691;
        } else {
            char near = ColorCode.nearest16(rgb);
            info = Lang.of("fwm.colorcodes.hexNearest", Lang.str("fwm.colorcodes.color." + near, new Object[0]), Character.valueOf(near));
            color = FwmTheme.TEXT_DIM;
        }
        ctx.text(font, ColorCodePanel.trimmed(font, (Component)info, this.contentW()), this.x, ry + 13 + 3, color, false);
        for (ColorRow row : this.hsvRows) {
            row.render(ctx, font, mouseX, this.inBody(mouseY) ? mouseY : -1000);
        }
    }

    private void renderImport(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        int top = this.at(317);
        this.heading(ctx, font, "fwm.colorcodes.import", top);
        int ry = top + 11;
        int loadW = this.loadW(font);
        int fieldW = this.contentW() - loadW - 4;
        FwmTheme.box(ctx, this.x, ry, fieldW, 13, FwmTheme.INSET, this.importField.isFocused() ? FwmTheme.ACCENT : FwmTheme.OFF);
        this.importField.extractWidgetRenderState(ctx, mouseX, mouseY, delta);
        int bx = this.x + fieldW + 4;
        ColorCodePanel.button(ctx, font, bx, ry, loadW, 13, (Component)Lang.of("fwm.colorcodes.importLoad", new Object[0]), this.inBody(mouseY) && ColorCodePanel.over(mouseX, mouseY, bx, ry, loadW, 13), false, !this.importText.isBlank());
    }

    private void renderScrollbar(GuiGraphicsExtractor ctx) {
        int max = this.maxScroll();
        int trackH = this.bodyHeight();
        if (max <= 0 || trackH <= 0) {
            return;
        }
        int trackX = this.x + this.width - 4;
        int thumbH = Math.max(10, trackH * trackH / 346);
        int thumbY = this.bodyTop() + (trackH - thumbH) * this.scroll / max;
        ctx.fill(trackX, this.bodyTop(), trackX + 4, this.y + this.height, FwmTheme.OFF);
        ctx.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, FwmTheme.ACCENT);
    }

    private void renderHoverTip(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int i;
        Object tip = null;
        int sw = this.swatchW();
        int colorsTop = this.at(37) + 11;
        for (int i2 = 0; i2 < 16 && tip == null; ++i2) {
            int sx = this.x + i2 % 8 * sw;
            int sy = colorsTop + i2 / 8 * 13;
            if (!ColorCodePanel.over(mouseX, mouseY, sx, sy, sw - 2, 11)) continue;
            char code = "0123456789abcdef".charAt(i2);
            tip = Lang.str("fwm.colorcodes.color." + code, new Object[0]) + "  &" + code;
        }
        int bw = this.formatW();
        int formatsTop = this.at(77) + 11;
        for (i = 0; i < FORMAT_KEYS.length && tip == null; ++i) {
            int bx = this.x + i % 3 * (bw + 4);
            int by = formatsTop + i / 3 * 16;
            if (!ColorCodePanel.over(mouseX, mouseY, bx, by, bw, 13)) continue;
            tip = Lang.str(FORMAT_KEYS[i], new Object[0]) + "  &" + (i < FORMATS.length ? ColorCodeEditorPanel.FORMATS[i].code : (char)'r');
        }
        for (i = 0; i < Gradient.PRESETS.size() && tip == null; ++i) {
            if (!ColorCodePanel.over(mouseX, mouseY, this.presetX(i), this.presetY(i), this.presetW(), 13)) continue;
            Gradient.Preset preset = Gradient.PRESETS.get(i);
            tip = Lang.str("fwm.colorcodes.gradientPreset", Lang.t(preset.de(), preset.en()));
        }
        int ry = this.gradientRowY();
        int toX = this.gradientToX(font);
        if (tip == null && ColorCodePanel.over(mouseX, mouseY, this.x, ry, 13, 13)) {
            tip = Lang.str("fwm.colorcodes.gradientFrom", new Object[0]);
        } else if (tip == null && ColorCodePanel.over(mouseX, mouseY, toX, ry, 13, 13)) {
            tip = Lang.str("fwm.colorcodes.gradientTo", new Object[0]);
        } else if (tip == null && ColorCodePanel.over(mouseX, mouseY, this.x + 15, ry, toX - this.x - 17, 13)) {
            tip = Lang.str("fwm.colorcodes.gradientSwap", new Object[0]);
        }
        if (tip == null) {
            return;
        }
        int tw = font.width(tip) + 8;
        int tx = Math.max(this.x, Math.min(this.x + this.width - tw, mouseX + 8));
        int ty = mouseY - 16 >= this.y ? mouseY - 16 : mouseY + 12;
        ColorCodePanel.tooltipBox(ctx, tx, ty, tw, 14);
        ctx.text(font, (String)tip, tx + 4, ty + 3, FwmTheme.TEXT, false);
    }

    private static ItemStack previewIcon(Item item) {
        try {
            return new ItemStack((ItemLike)item);
        }
        catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    private static MouseButtonEvent click(double mx, double my, int button) {
        return new MouseButtonEvent(mx, my, new MouseButtonInfo(button, 0));
    }

    private void clickField(EditBox field, double mx, double my, int button) {
        long now = System.currentTimeMillis();
        boolean doubled = field == this.lastClicked && now - this.lastClickAt < 250L;
        this.lastClicked = field;
        this.lastClickAt = now;
        this.focus(field);
        field.mouseClicked(ColorCodeEditorPanel.click(mx, my, button), doubled);
        this.dragging = field;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int i;
        if (this.name == null || !this.contains(mouseX, mouseY)) {
            return false;
        }
        if (button != 0) {
            return true;
        }
        this.position();
        Font font = Minecraft.getInstance().font;
        if (mouseY < (double)this.bodyTop()) {
            return this.clickTop(font, mouseX, mouseY, button);
        }
        for (ColorRow row : this.hsvRows) {
            if (!row.mouseClicked(mouseX, mouseY, button)) continue;
            this.draggingRow = row;
            return true;
        }
        int sw = this.swatchW();
        int colorsTop = this.at(37) + 11;
        for (int i2 = 0; i2 < 16; ++i2) {
            int sx = this.x + i2 % 8 * sw;
            int sy = colorsTop + i2 / 8 * 13;
            if (!ColorCodePanel.over(mouseX, mouseY, sx, sy, sw - 2, 11)) continue;
            String color = String.valueOf("0123456789abcdef".charAt(i2));
            this.onSelection((s, e) -> this.user.code.applyColor((int)s, (int)e, color));
            return true;
        }
        int bw = this.formatW();
        int formatsTop = this.at(77) + 11;
        for (i = 0; i < FORMAT_KEYS.length; ++i) {
            int bx = this.x + i % 3 * (bw + 4);
            int by = formatsTop + i / 3 * 16;
            if (!ColorCodePanel.over(mouseX, mouseY, bx, by, bw, 13)) continue;
            if (i < FORMATS.length) {
                CodeStyle.Format format = FORMATS[i];
                this.onSelection((s, e) -> this.user.code.toggleFormat((int)s, (int)e, format));
            } else {
                this.onSelection(this.user.code::reset);
            }
            return true;
        }
        for (i = 0; i < Gradient.PRESETS.size(); ++i) {
            if (!ColorCodePanel.over(mouseX, mouseY, this.presetX(i), this.presetY(i), this.presetW(), 13)) continue;
            this.applyGradient(Gradient.PRESETS.get(i).stops());
            return true;
        }
        int gy = this.gradientRowY();
        int toX = this.gradientToX(font);
        if (ColorCodePanel.over(mouseX, mouseY, this.x, gy, 13, 13)) {
            this.user.gradientFrom = this.currentHexRgb();
            return true;
        }
        if (ColorCodePanel.over(mouseX, mouseY, toX, gy, 13, 13)) {
            this.user.gradientTo = this.currentHexRgb();
            return true;
        }
        if (ColorCodePanel.over(mouseX, mouseY, this.x + 15, gy, toX - this.x - 17, 13)) {
            int from = this.user.gradientFrom;
            this.user.gradientFrom = this.user.gradientTo;
            this.user.gradientTo = from;
            return true;
        }
        if (ColorCodePanel.over(mouseX, mouseY, this.gradientApplyX(font), gy, this.gradientApplyW(font), 13)) {
            this.applyGradient(this.ownStops());
            return true;
        }
        int ry = this.hexRowY();
        if (ColorCodePanel.over(mouseX, mouseY, this.x + 17, ry, 52, 13)) {
            this.clickField(this.hex, mouseX, mouseY, button);
            return true;
        }
        if (ColorCodePanel.over(mouseX, mouseY, this.applyX(), ry, this.x + this.contentW() - this.applyX(), 13)) {
            this.applyHex();
            return true;
        }
        int iy = this.at(317) + 11;
        int loadW = this.loadW(font);
        int fieldW = this.contentW() - loadW - 4;
        if (ColorCodePanel.over(mouseX, mouseY, this.x, iy, fieldW, 13)) {
            this.clickField(this.importField, mouseX, mouseY, button);
            return true;
        }
        if (ColorCodePanel.over(mouseX, mouseY, this.x + fieldW + 4, iy, loadW, 13)) {
            this.importCode();
            return true;
        }
        return true;
    }

    private boolean clickTop(Font font, double mouseX, double mouseY, int button) {
        int mx = this.x;
        for (ColorCode.Mode m : ColorCode.Mode.values()) {
            int w = this.modeW(font, m);
            if (ColorCodePanel.over(mouseX, mouseY, mx, this.y, w, 13)) {
                this.user.mode = m;
                return true;
            }
            mx += w + 2;
        }
        int cw = this.copyW(font);
        if (ColorCodePanel.over(mouseX, mouseY, this.x + this.width - cw, this.y, cw, 13)) {
            this.copy();
            return true;
        }
        if (ColorCodePanel.over(mouseX, mouseY, this.x, this.y + 17, this.width, 16)) {
            this.clickField(this.name, mouseX, mouseY, button);
            return true;
        }
        return true;
    }

    private void copy() {
        ColorCode code = this.user.code;
        if (code.text().isEmpty() || code.tooLong(this.user.mode)) {
            return;
        }
        Minecraft.getInstance().keyboardHandler.setClipboard(code.command(this.user.mode));
        this.copiedAt = System.currentTimeMillis();
    }

    private void importCode() {
        if (this.importText.isBlank()) {
            return;
        }
        ColorCode.Imported imported = ColorCode.importCode(this.importText);
        this.user.load(imported.code(), imported.mode());
        this.syncFromSession();
        this.focus(this.name);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (this.draggingRow != null) {
            return this.draggingRow.mouseDragged(mouseX, mouseY, button);
        }
        if (this.dragging != null) {
            this.position();
            this.dragging.mouseDragged(ColorCodeEditorPanel.click(mouseX, mouseY, button), 0.0, 0.0);
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased() {
        if (this.draggingRow != null) {
            this.draggingRow.mouseReleased();
        }
        this.draggingRow = null;
        this.dragging = null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
        if (!this.inBody(mouseY)) {
            return false;
        }
        this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll - (int)Math.signum(vertical) * 14));
        return true;
    }

    private EditBox focused() {
        if (this.name == null) {
            return null;
        }
        if (this.name.isFocused()) {
            return this.name;
        }
        if (this.hex.isFocused()) {
            return this.hex;
        }
        if (this.importField.isFocused()) {
            return this.importField;
        }
        return null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        EditBox field = this.focused();
        if (field == null) {
            return false;
        }
        if (keyCode == 257 || keyCode == 335) {
            if (field == this.hex) {
                this.applyHex();
            }
            if (field == this.importField) {
                this.importCode();
            }
            return true;
        }
        return field.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        EditBox field = this.focused();
        return field != null && field.charTyped(input);
    }
}

