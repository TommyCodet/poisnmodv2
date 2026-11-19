/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.net.SpawnerData;
import net.friendswithmoney.fwm.net.Spawners;
import net.friendswithmoney.fwm.ui.HudEditScreen;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.SpawnerCard;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.panel.SpawnerPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.ChipsRow;
import net.friendswithmoney.fwm.ui.widget.HexRow;
import net.friendswithmoney.fwm.ui.widget.PriceRow;
import net.friendswithmoney.fwm.ui.widget.SectionRow;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.TextInputRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SpawnerHudPanel
extends RowsPanel {
    private String open = "prices";

    @Override
    public Component title() {
        return Lang.of("fwm.tab.spawnerhud", new Object[0]);
    }

    private static Component t(String de, String en) {
        return Component.literal((String)Lang.t(de, en));
    }

    private void heading(String de, String en) {
        this.rows.add(new TextRow(SpawnerHudPanel.t(de, en)));
    }

    private void toggle(String de, String en, BooleanSupplier get, Consumer<Boolean> set) {
        this.rows.add(new ToggleRow(SpawnerHudPanel.t(de, en), get, v -> {
            set.accept((Boolean)v);
            ConfigManager.save();
        }));
    }

    private void slider(String de, String en, int min, int max, IntSupplier get, IntConsumer set, String unit) {
        this.rows.add(new SliderRow(SpawnerHudPanel.t(de, en), min, max, get, v -> {
            set.accept(v);
            ConfigManager.save();
        }, v -> Component.literal((String)(v + unit))));
    }

    private void color(String de, String en, IntSupplier get, IntConsumer set) {
        this.rows.add(new HexRow(SpawnerHudPanel.t(de, en), get, v -> {
            set.accept(v);
            ConfigManager.save();
        }));
    }

    private void words(String de, String en, Supplier<String> get, Consumer<String> set, String empty) {
        this.rows.add(new TextInputRow(SpawnerHudPanel.t(de, en), get, v -> {
            set.accept((String)v);
            ConfigManager.save();
        }, empty, 32));
    }

    private void section(String id, String de, String en, String hintDe, String hintEn) {
        this.rows.add(new SectionRow(SpawnerHudPanel.t(de, en), Lang.t(hintDe, hintEn), () -> id.equals(this.open), () -> {
            this.open = id.equals(this.open) ? "" : id;
            this.onResize();
        }));
    }

    @Override
    protected void buildRows() {
        String name;
        FwmConfig c = FwmConfig.INSTANCE;
        this.toggle("Anzeige im Spiel", "Show in game", () -> c.spawnerHudCard, v -> {
            c.spawnerHudCard = v;
        });
        this.rows.add(new ButtonRow(SpawnerHudPanel.t("Position und Gr\u00f6\u00dfe", "Position and size"), () -> SpawnerHudPanel.t("HUD anpassen", "Adjust HUD"), () -> Minecraft.getInstance().gui.setScreen((Screen)new HudEditScreen(Minecraft.getInstance().gui.screen()))));
        SpawnerData data = Spawners.data();
        List<SpawnerData.Item> items = data == null || data.items == null ? List.of() : SpawnerPanel.inShopOrder(data.items);
        this.section("prices", "Eigene Preise", "Own prices", "leer = Preis der Website", "empty = website price");
        if ("prices".equals(this.open)) {
            if (items.isEmpty()) {
                this.heading("Spawner werden geladen \u2026", "Loading spawners \u2026");
            }
            for (SpawnerData.Item item : items) {
                name = item.name;
                this.rows.add(new PriceRow((Component)Component.literal((String)name), () -> item.buyPrice, () -> item.sellPrice, () -> c.spawnerHudPrices.get(name), v -> {
                    boolean empty;
                    boolean bl = empty = !(v[0] != null && !v[0].isBlank() || v[1] != null && !v[1].isBlank());
                    if (empty) {
                        c.spawnerHudPrices.remove(name);
                    } else {
                        c.spawnerHudPrices.put(name, (String[])v);
                    }
                    ConfigManager.save();
                }));
            }
        }
        this.section("look", "Aussehen", "Look", "Stil, Hintergrund, Rahmen", "style, background, border");
        if ("look".equals(this.open)) {
            this.rows.add(new ChipsRow(SpawnerHudPanel.t("Stil", "Style"), List.of(new ChipsRow.Chip("FWM", 0), new ChipsRow.Chip("Apple", 1), new ChipsRow.Chip("Neon", 2), new ChipsRow.Chip("Minimal", 3)), () -> c.spawnerHudStyle, v -> {
                SpawnerCard.applyStyle(v);
                ConfigManager.save();
            }));
            this.rows.add(new ChipsRow(SpawnerHudPanel.t("Hintergrund", "Background"), List.of(new ChipsRow.Chip(Lang.t("Dunkel", "Solid"), 0), new ChipsRow.Chip(Lang.t("Glas", "Glass"), 1), new ChipsRow.Chip(Lang.t("Keiner", "None"), 2)), () -> c.spawnerHudBg, v -> {
                c.spawnerHudBg = v;
                ConfigManager.save();
            }));
            this.rows.add(new ChipsRow(SpawnerHudPanel.t("Rahmen", "Border"), List.of(new ChipsRow.Chip(Lang.t("Aus", "Off"), 0), new ChipsRow.Chip(Lang.t("Fein", "Thin"), 1), new ChipsRow.Chip(Lang.t("Farbverlauf", "Gradient"), 2)), () -> c.spawnerHudBorder, v -> {
                c.spawnerHudBorder = v;
                ConfigManager.save();
            }));
            this.rows.add(new ChipsRow((Component)Component.literal((String)"Layout"), List.of(new ChipsRow.Chip(Lang.t("Liste", "List"), 0), new ChipsRow.Chip(Lang.t("Raster", "Grid"), 1)), () -> c.spawnerHudGrid ? 1 : 0, v -> {
                c.spawnerHudGrid = v == 1;
                ConfigManager.save();
            }));
        }
        this.section("content", "Inhalt", "Content", "Titel, Spalten, Bilder", "title, columns, pictures");
        if ("content".equals(this.open)) {
            this.words("Titel", "Title", () -> c.spawnerHudTitle, v -> {
                c.spawnerHudTitle = v;
            }, Lang.t("kein Titel", "no title"));
            this.toggle("Kopfzeile", "Header", () -> c.spawnerHudHeader, v -> {
                c.spawnerHudHeader = v;
            });
            this.words("Spalte Name", "Name column", () -> c.spawnerHudLabelName, v -> {
                c.spawnerHudLabelName = v;
            }, "Spawner");
            this.words("Spalte Kauf", "Buy column", () -> c.spawnerHudLabelBuy, v -> {
                c.spawnerHudLabelBuy = v;
            }, "Kauf");
            this.words("Spalte Verkauf", "Sell column", () -> c.spawnerHudLabelSell, v -> {
                c.spawnerHudLabelSell = v;
            }, "Verkauf");
            this.toggle("Kauf-Preise", "Buy prices", () -> c.spawnerHudShowBuy, v -> {
                c.spawnerHudShowBuy = v;
            });
            this.toggle("Verkauf-Preise", "Sell prices", () -> c.spawnerHudShowSell, v -> {
                c.spawnerHudShowSell = v;
            });
            this.toggle("Bilder der Spawner", "Spawner pictures", () -> c.spawnerHudIcons, v -> {
                c.spawnerHudIcons = v;
            });
        }
        this.section("spawners", "Spawner ausw\u00e4hlen", "Choose spawners", items.size() - c.spawnerHudHidden.size() + " von " + items.size(), items.size() - c.spawnerHudHidden.size() + " of " + items.size());
        if ("spawners".equals(this.open)) {
            for (SpawnerData.Item item : items) {
                name = item.name;
                this.rows.add(new ToggleRow((Component)Component.literal((String)name), () -> !c.spawnerHudHidden.contains(name), v -> {
                    if (v.booleanValue()) {
                        c.spawnerHudHidden.remove(name);
                    } else if (!c.spawnerHudHidden.contains(name)) {
                        c.spawnerHudHidden.add(name);
                    }
                    ConfigManager.save();
                }));
            }
        }
        this.section("colors", "Farben", "Colours", "Kauf, Verkauf, Text \u2026", "buy, sell, text \u2026");
        if ("colors".equals(this.open)) {
            this.color("Kauf", "Buy", () -> c.spawnerHudBuyColor, v -> {
                c.spawnerHudBuyColor = v;
            });
            this.color("Verkauf", "Sell", () -> c.spawnerHudSellColor, v -> {
                c.spawnerHudSellColor = v;
            });
            this.color("Namen", "Names", () -> c.spawnerHudTextColor, v -> {
                c.spawnerHudTextColor = v;
            });
            this.color("Kopfzeile", "Header", () -> c.spawnerHudHeadColor, v -> {
                c.spawnerHudHeadColor = v;
            });
            this.color("Hintergrund", "Background", () -> c.spawnerHudBgColor, v -> {
                c.spawnerHudBgColor = v;
            });
            this.color("Rahmen", "Border", () -> c.spawnerHudBorderColor, v -> {
                c.spawnerHudBorderColor = v;
            });
        }
        this.section("fine", "Feinschliff", "Fine-tuning", "Deckkraft, Ecken, Abst\u00e4nde \u2026", "opacity, corners, spacing \u2026");
        if ("fine".equals(this.open)) {
            this.slider("Deckkraft", "Opacity", 10, 100, () -> c.spawnerHudOpacity, v -> {
                c.spawnerHudOpacity = v;
            }, " %");
            this.slider("Ecken", "Corners", 0, 12, () -> c.spawnerHudRadius, v -> {
                c.spawnerHudRadius = v;
            }, " px");
            this.slider("Innenabstand", "Padding", 2, 16, () -> c.spawnerHudPadding, v -> {
                c.spawnerHudPadding = v;
            }, " px");
            this.slider("Zeilenh\u00f6he", "Row height", 10, 20, () -> c.spawnerHudRowGap, v -> {
                c.spawnerHudRowGap = v;
            }, " px");
            this.slider("Rahmenst\u00e4rke", "Border width", 1, 3, () -> c.spawnerHudBorderWidth, v -> {
                c.spawnerHudBorderWidth = v;
            }, " px");
            this.slider("Tempo Farbverlauf", "Gradient speed", 1, 10, () -> c.spawnerHudGradientSpeed, v -> {
                c.spawnerHudGradientSpeed = v;
            }, "");
            this.toggle("Schatten unter der Karte", "Shadow under the card", () -> c.spawnerHudShadow, v -> {
                c.spawnerHudShadow = v;
            });
            this.toggle("Textschatten", "Text shadow", () -> c.spawnerHudTextShadow, v -> {
                c.spawnerHudTextShadow = v;
            });
            this.toggle("Trennlinie", "Divider line", () -> c.spawnerHudDivider, v -> {
                c.spawnerHudDivider = v;
            });
            this.toggle("Kopfzeile fett und farbig", "Header bold and coloured", () -> c.spawnerHudHeadBold, v -> {
                c.spawnerHudHeadBold = v;
            });
        }
    }
}

