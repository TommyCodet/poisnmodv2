/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 */
package net.friendswithmoney.fwm.net;

import com.google.gson.Gson;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.FwmClient;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.net.FwmApi;
import net.friendswithmoney.fwm.ui.FwmNotice;
import net.friendswithmoney.fwm.ui.Lang;
import net.minecraft.client.Minecraft;

public final class UpdateCheck {
    private static final String PROJECT = "nxQeqT7s";
    private static final long FIRST_DELAY_MS = 20000L;
    private static final long RETRY_MS = 21600000L;
    private static final Gson GSON = new Gson();
    private static long nextCheck;
    private static boolean running;
    private static boolean doneThisRun;

    private UpdateCheck() {
    }

    public static void tick(Minecraft client) {
        if (doneThisRun || running || client.player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (nextCheck == 0L) {
            nextCheck = now + 20000L;
        }
        if (now < nextCheck) {
            return;
        }
        running = true;
        nextCheck = now + 21600000L;
        UpdateCheck.check();
    }

    private static void check() {
        String mc = UpdateCheck.mcVersion();
        String url = "https://api.modrinth.com/v2/project/nxQeqT7s/version?loaders=%5B%22fabric%22%5D&game_versions=%5B%22" + mc + "%22%5D";
        FwmApi.getUrlStreamed(url, reader -> (Version[])GSON.fromJson(reader, Version[].class)).whenComplete((versions, error) -> {
            running = false;
            if (error != null || versions == null || ((Version[])versions).length == 0) {
                return;
            }
            String latest = null;
            for (Version v : versions) {
                if (v.version_number == null || v.game_versions != null && !v.game_versions.contains(mc)) continue;
                latest = v.version_number;
                break;
            }
            if (latest == null) {
                return;
            }
            String current = FwmClient.MOD_VERSION;
            if (!UpdateCheck.newer(latest, current)) {
                return;
            }
            if (latest.equals(V2.INSTANCE.updateAnnounced)) {
                return;
            }
            doneThisRun = true;
            V2.INSTANCE.updateAnnounced = latest;
            V2.save();
            String shown = UpdateCheck.strip(latest);
            Minecraft.getInstance().execute(() -> FwmNotice.show("FWM Mod", Lang.t("Update verf\u00fcgbar", "Update available"), Lang.t("Neu: ", "New: ") + shown + Lang.t(" \u00b7 auf Modrinth", " \u00b7 on Modrinth"), 16103723, 8000L));
        });
    }

    public static boolean newerThanThis(String version) {
        return version != null && !version.isBlank() && UpdateCheck.newer(version, FwmClient.MOD_VERSION);
    }

    private static String mcVersion() {
        return FabricLoader.getInstance().getModContainer("minecraft").map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("");
    }

    static boolean newer(String candidate, String current) {
        int[] a = UpdateCheck.numbers(candidate);
        int[] b = UpdateCheck.numbers(current);
        for (int i = 0; i < Math.max(a.length, b.length); ++i) {
            int y;
            int x = i < a.length ? a[i] : 0;
            int n = y = i < b.length ? b[i] : 0;
            if (x == y) continue;
            return x > y;
        }
        return false;
    }

    private static int[] numbers(String version) {
        String head = UpdateCheck.strip(version);
        String[] parts = head.split("\\.");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; ++i) {
            try {
                out[i] = Integer.parseInt(parts[i]);
                continue;
            }
            catch (NumberFormatException e) {
                out[i] = 0;
            }
        }
        return out;
    }

    private static String strip(String version) {
        int plus = version.indexOf(43);
        return plus < 0 ? version : version.substring(0, plus);
    }

    static final class Version {
        String version_number;
        List<String> game_versions;
        List<String> loaders;

        Version() {
        }
    }
}

