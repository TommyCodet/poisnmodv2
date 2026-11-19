/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.border.WorldBorder
 */
package net.friendswithmoney.fwm.antileak;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;

public final class FakeCoords {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final double TELEPORT = 24.0;
    private static final double EDGE = 200.0;
    private static final double RANGE = 250000.0;
    private static final Pattern XYZ = Pattern.compile("^XYZ: .*");
    private static final Pattern BLOCK = Pattern.compile("^Block: -?\\d+ -?\\d+ -?\\d+");
    private static final Pattern CHUNK = Pattern.compile("^Chunk: .*");
    private static final Pattern SECTION = Pattern.compile("^Section-relative: .*");
    private static final Pattern TARGETED = Pattern.compile("(Targeted (?:Block|Fluid): )(-?\\d+), (-?\\d+), (-?\\d+)");
    private static Level world;
    private static boolean started;
    private static double realX;
    private static double realY;
    private static double realZ;
    private static double fakeX;
    private static double fakeY;
    private static double fakeZ;
    private static double heading;
    private static double turn;

    private FakeCoords() {
    }

    public static boolean active() {
        return Features.isOn("fwm.feature.antileak") && FwmConfig.INSTANCE.antiLeakCoords;
    }

    public static List<String> rewrite(List<String> lines) {
        if (lines == null || !FakeCoords.active()) {
            return lines;
        }
        Entity camera = Minecraft.getInstance().getCameraEntity();
        if (camera == null) {
            return lines;
        }
        FakeCoords.follow(camera);
        int bx = FakeCoords.floor(fakeX);
        int by = FakeCoords.floor(fakeY);
        int bz = FakeCoords.floor(fakeZ);
        int realBx = camera.getBlockX();
        int realBy = camera.getBlockY();
        int realBz = camera.getBlockZ();
        ArrayList<String> out = new ArrayList<String>(lines.size());
        for (String line : lines) {
            if (line == null) {
                out.add(null);
                continue;
            }
            String plain = line.replaceAll("\u00a7.", "");
            if (XYZ.matcher(plain).matches()) {
                out.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", fakeX, fakeY, fakeZ));
                continue;
            }
            if (BLOCK.matcher(plain).lookingAt()) {
                String inChunk = plain.contains("[") ? String.format(Locale.ROOT, " [%d %d %d]", bx & 0xF, by & 0xF, bz & 0xF) : "";
                out.add(String.format(Locale.ROOT, "Block: %d %d %d", bx, by, bz) + inChunk);
                continue;
            }
            if (CHUNK.matcher(plain).matches()) {
                int cx = bx >> 4;
                int cz = bz >> 4;
                out.add(String.format(Locale.ROOT, "Chunk: %d %d %d [%d %d in r.%d.%d.mca]", cx, by >> 4, cz, cx & 0x1F, cz & 0x1F, cx >> 5, cz >> 5));
                continue;
            }
            if (SECTION.matcher(plain).matches()) {
                out.add(String.format(Locale.ROOT, "Section-relative: %02d %02d %02d", bx & 0xF, by & 0xF, bz & 0xF));
                continue;
            }
            Matcher m = TARGETED.matcher(line);
            if (m.find()) {
                int tx = bx + Integer.parseInt(m.group(2)) - realBx;
                int ty = by + Integer.parseInt(m.group(3)) - realBy;
                int tz = bz + Integer.parseInt(m.group(4)) - realBz;
                out.add(m.replaceFirst(Matcher.quoteReplacement(m.group(1) + tx + ", " + ty + ", " + tz)));
                continue;
            }
            out.add(line);
        }
        return out;
    }

    private static void follow(Entity camera) {
        double x = camera.getX();
        double y = camera.getY();
        double z = camera.getZ();
        if (!started || camera.level() != world) {
            FakeCoords.restart(camera, x, y, z);
            return;
        }
        double dx = x - realX;
        double dy = y - realY;
        double dz = z - realZ;
        realX = x;
        realY = y;
        realZ = z;
        double step = Math.sqrt(dx * dx + dz * dz);
        if (step > 24.0 || Math.abs(dy) > 24.0) {
            FakeCoords.restart(camera, x, y, z);
            return;
        }
        if (step > 1.0E-6) {
            turn = turn * 0.95 + RANDOM.nextGaussian() * 0.01;
            if (RANDOM.nextInt(400) == 0) {
                turn += (RANDOM.nextDouble() - 0.5) * 0.6;
            }
            fakeX += Math.cos(heading += turn) * step;
            fakeZ += Math.sin(heading) * step;
        }
        fakeY += dy;
    }

    private static void restart(Entity camera, double x, double y, double z) {
        world = camera.level();
        started = true;
        realX = x;
        realY = y;
        realZ = z;
        WorldBorder border = world.getWorldBorder();
        double half = Math.max(400.0, Math.min(border.getSize() / 2.0, 250000.0) - 200.0);
        fakeX = border.getCenterX() + (RANDOM.nextDouble() * 2.0 - 1.0) * half;
        fakeZ = border.getCenterZ() + (RANDOM.nextDouble() * 2.0 - 1.0) * half;
        int bottom = world.getMinY();
        fakeY = (double)(bottom + 8 + RANDOM.nextInt(Math.max(16, 180 - bottom))) + RANDOM.nextDouble();
        heading = RANDOM.nextDouble() * Math.PI * 2.0;
        turn = 0.0;
    }

    private static int floor(double value) {
        return (int)Math.floor(value);
    }
}

