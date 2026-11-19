/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 */
package net.friendswithmoney.fwm.textures;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

public final class BuiltinPacks {
    public static final String PREFIX = "fwm-pack:";
    private static final List<String> NAMES = List.of("Gold Pack", "Clean Pack", "Violet Wind", "Milky Way", "Ashen", "Swight", "King in Yellow", "Fallen Angel", "Pig White", "Pig Purple", "Pig Error", "Pig Black Glitch", "Pink Purplish", "Midnight Gotham", "Cloudy Nights", "Better Sky");
    private static boolean extracted;

    private BuiltinPacks() {
    }

    public static boolean is(String choice) {
        return choice != null && choice.startsWith(PREFIX);
    }

    public static String label(String choice) {
        return choice.substring(PREFIX.length());
    }

    public static List<String> ids() {
        BuiltinPacks.ensure();
        ArrayList<String> out = new ArrayList<String>();
        for (String name : NAMES) {
            if (!Files.exists(BuiltinPacks.file(PREFIX + name), new LinkOption[0])) continue;
            out.add(PREFIX + name);
        }
        return out;
    }

    private static Path dir() {
        return FabricLoader.getInstance().getConfigDir().resolve("fwm").resolve("packs");
    }

    public static Path file(String choice) {
        return BuiltinPacks.dir().resolve(BuiltinPacks.label(choice) + ".zip");
    }

    private static void ensure() {
        if (extracted) {
            return;
        }
        extracted = true;
        try {
            Files.createDirectories(BuiltinPacks.dir(), new FileAttribute[0]);
        }
        catch (IOException e) {
            System.err.println("[FWM] Built-in packs: no folder: " + String.valueOf(e));
            return;
        }
        for (String name : NAMES) {
            try {
                InputStream in = BuiltinPacks.class.getResourceAsStream("/fwm-packs/" + name + ".zip");
                try {
                    if (in == null) continue;
                    byte[] data = in.readAllBytes();
                    Path target = BuiltinPacks.dir().resolve(name + ".zip");
                    if (Files.exists(target, new LinkOption[0]) && Files.size(target) == (long)data.length) continue;
                    Path tmp = BuiltinPacks.dir().resolve(name + ".zip.tmp");
                    Files.write(tmp, data, new OpenOption[0]);
                    Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
                }
                finally {
                    if (in == null) continue;
                    in.close();
                }
            }
            catch (IOException e) {
                System.err.println("[FWM] Built-in pack " + name + " could not be copied: " + String.valueOf(e));
            }
        }
    }
}

