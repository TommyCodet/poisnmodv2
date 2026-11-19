/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.FormattedCharSequence
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package net.friendswithmoney.fwm.lyrics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class Sink {
    final SubmitNodeCollector queue;

    Sink(SubmitNodeCollector queue) {
        this.queue = queue;
    }

    private static PoseStack at(Matrix4f m) {
        PoseStack p = new PoseStack();
        p.last().pose().set((Matrix4fc)m);
        return p;
    }

    static void draw(Font font, Component text, float x, float y, int color, boolean shadow, Matrix4f m, Sink sink, Font.DisplayMode mode, int background, int light) {
        Sink.draw(font, text.getVisualOrderText(), x, y, color, shadow, m, sink, mode, background, light);
    }

    static void draw(Font font, FormattedCharSequence text, float x, float y, int color, boolean shadow, Matrix4f m, Sink sink, Font.DisplayMode mode, int background, int light) {
        if (sink == null || sink.queue == null) {
            return;
        }
        sink.queue.submitText(Sink.at(m), x, y, text, shadow, mode, light, color, background, 0);
    }

    static void outline(Font font, FormattedCharSequence text, float x, float y, int color, int outline, Matrix4f m, Sink sink, int light) {
        if (sink == null || sink.queue == null) {
            return;
        }
        sink.queue.submitText(Sink.at(m), x, y, text, false, Font.DisplayMode.NORMAL, light, color, 0, outline);
    }

    void quads(RenderType type, Consumer<VertexConsumer> body) {
        if (this.queue == null) {
            return;
        }
        this.queue.submitCustomGeometry(new PoseStack(), type, (pose, consumer) -> body.accept(consumer));
    }
}

