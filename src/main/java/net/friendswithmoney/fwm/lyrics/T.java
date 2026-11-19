/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.lyrics;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.friendswithmoney.fwm.ui.Lang;

public final class T {
    private static final Map<String, String> M = new HashMap<String, String>();

    private T() {
    }

    public static boolean de() {
        return Lang.isGerman();
    }

    public static String tr(String var0) {
        return var0 != null && !T.de() ? M.getOrDefault(var0, var0) : var0;
    }

    public static Locale locale() {
        return T.de() ? Locale.GERMAN : Locale.ENGLISH;
    }

    static {
        M.put("Songtext aus (nur lokal)", "Lyrics off (local only)");
        M.put("Startseite", "Home");
        M.put("PvP-Rand", "PvP outline");
        M.put("Wie dick der Rand um die PvP-Lyrics ist. 0 = kein Rand.", "How thick the outline around the PvP lyrics is. 0 = no outline.");
        M.put("Farben", "Colors");
        M.put("Farben, Looks und Schriften", "Colors, looks and fonts");
        M.put("Alle Schriften anzeigen", "Show all fonts");
        M.put("Looks", "Looks");
        M.put("Fertige Stile und Farbverlauf", "Ready-made styles and color gradient");
        M.put("Schriften", "Fonts");
        M.put("Schriftart f\u00fcr den Himmelstext", "Font for the sky lyrics");
        M.put("Farbverlauf", "Color gradient");
        M.put("Textfarbe \u2192 zweite Farbe", "text color \u2192 second color");
        M.put("Die Zeile l\u00e4uft von deiner Textfarbe (Stil) zur zweiten Farbe. Wirkt am besten mit Himmelsfarbe \"Eigene\".", "Each line fades from your text color (Style) to the second color. Works best with sky color \"Custom\".");
        M.put("Leuchten", "Glow");
        M.put("Farbiger Schein um die Buchstaben. 0 = aus.", "Colored glow around the letters. 0 = off.");
        M.put("Schwung", "Swing");
        M.put("Zeilen leicht gebogen", "lines slightly curved");
        M.put("Die Zeilen stehen als leichter Bogen am Himmel, wie von Hand geschrieben.", "Lines appear as a gentle curve in the sky, like handwriting.");
        M.put("Farbe 2", "Color 2");
        M.put("Windows-Schrift \u2013 wird beim ersten Mal als Ressourcenpaket \"SkyLyrics-Schriften\" aktiviert.", "Windows font \u2013 enabled as resource pack \"SkyLyrics-Schriften\" the first time.");
        M.put("Klar", "Clean");
        M.put("Lavendel", "Lavender");
        M.put("Vertr\u00e4umt", "Dreamy");
        M.put("Kampf", "Fight");
        M.put("Feuer", "Fire");
        M.put("Eis", "Ice");
        M.put("Handschrift", "Handwriting");
        M.put("Lora Kursiv", "Lora Italic");
        M.put("Georgia Kursiv", "Georgia Italic");
        M.put("Was gerade l\u00e4uft", "What's playing right now");
        M.put("Himmel", "Sky");
        M.put("Wo und wie die Lyrics am Himmel stehen", "Where and how the lyrics appear in the sky");
        M.put("Stil", "Style");
        M.put("Farben, Schrift und die Anzeige oben", "Colors, font and the now-playing bar");
        M.put("Feuerwerk, Blitze und Licht zur Musik", "Fireworks, lightning and lights to the music");
        M.put("Wetter & PvP", "Weather & PvP");
        M.put("Wetter + Uhrzeit nach Genre, Lyrics beim Gegner", "Weather + time by genre, lyrics next to your opponent");
        M.put("AFK-Kino", "AFK Cinema");
        M.put("Kino-Shots von deinem Skin, wenn du AFK bist", "Cinematic shots of your skin while you're AFK");
        M.put("Sicher? Nochmal!", "Sure? Click again!");
        M.put("\u21ba Standard", "\u21ba Defaults");
        M.put("Setzt alle Einstellungen der Mod (Sky Lyrics, Effekte, AFK-Kino) auf Standard zur\u00fcck.", "Resets all settings of the mod (Sky Lyrics, effects, AFK Cinema) to their defaults.");
        M.put("Vorheriger Song", "Previous song");
        M.put("N\u00e4chster Song", "Next song");
        M.put("Lyrics fr\u00fcher (\u2212250 ms) \u2013 wird f\u00fcr diesen Song gemerkt", "Lyrics earlier (\u2212250 ms) \u2013 remembered for this song");
        M.put("Lyrics sp\u00e4ter (+250 ms) \u2013 wird f\u00fcr diesen Song gemerkt", "Lyrics later (+250 ms) \u2013 remembered for this song");
        M.put("\u21bb Neu laden", "\u21bb Reload");
        M.put("Sucht den Songtext nochmal (auch wenn er schon gefunden oder als 'keine Lyrics' markiert wurde).", "Searches for the lyrics again (even if they were already found or marked as 'no lyrics').");
        M.put("Holt die Zeit neu von Spotify und richtet den Songtext sofort daran aus. Passiert sonst auch automatisch.", "Gets the current time from Spotify again and re-aligns the lyrics right away. This also happens automatically.");
        M.put("\u2726 Feuerwerk", "\u2726 Fireworks");
        M.put("Text im Himmel", "Text in the sky");
        M.put("Now-Playing-Anzeige", "Now-playing bar");
        M.put("Modus", "Mode");
        M.put("Aufbauen", "Build up");
        M.put("Wegsingen", "Sing away");
        M.put("Vorbeilaufen", "Walk past");
        M.put("Aufbauen: jedes Wort erscheint beim Singen. Wegsingen: die Zeile steht schon da, W\u00f6rter verschwinden. Vorbeilaufen: jede Zeile taucht abwechselnd links und rechts vor dir auf dem Boden auf (beim Rennen weiter vorne) und verschwindet, wenn du daran vorbei bist.", "Build up: every word appears as it is sung. Sing away: the whole line is there, sung words disappear. Walk past: each line appears on the ground, alternating left and right in front of you (further ahead when sprinting), and fades out once you pass it.");
        M.put("Entfernung", "Distance");
        M.put(" Bl\u00f6cke", " blocks");
        M.put("Wie weit weg der Text im Himmel steht. Die Schrift w\u00e4chst mit, damit sie gleich gro\u00df wirkt.", "How far away the text is placed in the sky. The font scales along, so it always looks the same size.");
        M.put("H\u00f6he", "Height");
        M.put("Gr\u00f6\u00dfe", "Size");
        M.put("Zuf\u00e4llige Position", "Random position");
        M.put("jede Zeile woanders im Blickfeld", "every line somewhere else in view");
        M.put("Immer sichtbar", "Always visible");
        M.put("weicht Wasser und Bl\u00f6cken aus", "avoids water and blocks");
        M.put("An Tempo anpassen", "Adapt to speed");
        M.put("schneller = Text weiter vorne", "faster = text further ahead");
        M.put("Nur unter freiem Himmel", "Only under open sky");
        M.put("Nur im AFK-Kino", "Only in AFK Cinema");
        M.put("sonst kein Himmelstext", "no sky text otherwise");
        M.put("An: Der Songtext am Himmel erscheint nur, wenn das AFK-Kino l\u00e4uft. Beim normalen Spielen bleibt der Himmel frei (Now-Playing-Anzeige, Effekte und PvP-Edit bleiben).", "On: the lyrics in the sky only appear while AFK Cinema is running. During normal play the sky stays clear (now-playing bar, effects and PvP Edit stay active).");
        M.put("Farbe f\u00fcr", "Color for");
        M.put("Anzeige oben", "Top bar");
        M.put("Himmelstext", "Sky text");
        M.put("Farbton", "Hue");
        M.put("Himmelsfarbe", "Sky color");
        M.put("Hell", "Light");
        M.put("Dunkel", "Dark");
        M.put("Eigene", "Custom");
        M.put("Auto = hell bei Nacht, dunkel bei Tag \u00b7 Eigene = deine Farbe von oben", "Auto = light at night, dark during the day \u00b7 Custom = your color from above");
        M.put("Hintergrund", "Background");
        M.put("Schrift wechseln. Schreibschriften kommen aus Windows und werden beim ersten Mal als Ressourcenpaket \"SkyLyrics-Schriften\" aktiviert.", "Change the font. Handwriting fonts come from Windows and are enabled as the resource pack \"SkyLyrics-Schriften\" the first time.");
        M.put("Fett", "Bold");
        M.put("Kursiv", "Italic");
        M.put("Effekt", "Effect");
        M.put("Keiner", "None");
        M.put("Schatten", "Shadow");
        M.put("Umriss", "Outline");
        M.put("Anzeige", "Bar");
        M.put("Oben", "Top");
        M.put("Unten", "Bottom");
        M.put("Wo die Now-Playing-Leiste im Spiel steht.", "Where the now-playing bar is shown in game.");
        M.put("Text und Welt pulsieren im Takt: der Text pumpt auf jeden Schlag, Funken springen raus, im Refrain gibt's Druckwellen am Boden. H\u00f6rt nur Spotify ab, nicht Minecraft.", "Text and world pulse to the beat: the text pumps on every beat, sparks fly out and the chorus sends shockwaves across the ground. Only listens to Spotify, not to Minecraft.");
        M.put("W\u00f6rter zerfallen", "Dissolving words");
        M.put("Staub + gl\u00fchende lange T\u00f6ne", "dust + glowing long notes");
        M.put("Gesungene W\u00f6rter zerfallen in Partikel und wehen weg. Lang gehaltene W\u00f6rter leuchten und wachsen, solange sie gesungen werden.", "Sung words break into particles and drift away. Long held words glow and grow while they are sung.");
        M.put("Songwechsel-Intro", "Song intro");
        M.put("Blitz + Titel am Himmel", "flash + title in the sky");
        M.put("Beim Songwechsel zerf\u00e4llt der alte Text, ein kurzer Lichtblitz kommt und der neue Titel baut sich Buchstabe f\u00fcr Buchstabe am Himmel auf.", "When the song changes, the old text dissolves, a short flash of light appears and the new title builds up letter by letter in the sky.");
        M.put("Feuerwerk", "Fireworks");
        M.put("Songstart, Refrain, Drop, Finale", "song start, chorus, drop, finale");
        M.put("Raketen nur an gro\u00dfen Momenten im Song, mit Pause dazwischen. Farben vom Album-Cover, Form vom Genre.", "Rockets only at the big moments of a song, with a break in between. Colors from the album cover, shape from the genre.");
        M.put("Intensit\u00e4t", "Intensity");
        M.put("Dezent", "Subtle");
        M.put("Episch", "Epic");
        M.put("Wie viele Raketen pro Moment und wie lange Pause dazwischen.", "How many rockets per moment and how long the break in between is.");
        M.put("Blitz-Drops", "Lightning drops");
        M.put("bei Rock, Metal, Techno \u2026", "for rock, metal, techno \u2026");
        M.put("Beim Refrain oder nach einer Pause schl\u00e4gt am Horizont ein Blitz ein \u2013 nur bei Gewitter-Genres. Nur bei dir, ohne Feuer.", "On the chorus or after a break, lightning strikes on the horizon \u2013 only for thunderstorm genres. Only visible to you, no fire.");
        M.put("Lichts\u00e4ulen", "Light pillars");
        M.put("beim Finale des Songs", "at the finale of the song");
        M.put("Bei der letzten Zeile steigen Lichts\u00e4ulen im Kreis um dich auf.", "On the last line, pillars of light rise in a circle around you.");
        M.put("Noten-Aura", "Note aura");
        M.put("im Refrain, pro Wort", "in the chorus, per word");
        M.put("W\u00e4hrend der Refrain gesungen wird, steigt bei jedem Wort eine bunte Note neben dir auf.", "While the chorus is sung, a colorful note rises next to you on every word.");
        M.put("Sternschnuppen", "Shooting stars");
        M.put("nachts, alle 20\u201340 s", "at night, every 20\u201340 s");
        M.put("\u2726 Alles testen", "\u2726 Test all");
        M.put("Zeigt alle eingeschalteten Effekte einmal.", "Shows all enabled effects once.");
        M.put("\u2726 Intro testen", "\u2726 Test intro");
        M.put("Spielt das Songwechsel-Intro mit dem aktuellen Song ab (Musik muss laufen).", "Plays the song intro with the current song (music must be playing).");
        M.put("Welt an Musik anpassen", "Match world to music");
        M.put("Wetter + Uhrzeit", "weather + time");
        M.put("Hauptschalter: Aus = Wetter und Uhrzeit bleiben wie auf dem Server. An = die beiden Schalter darunter entscheiden.", "Main switch: Off = weather and time stay as on the server. On = the two switches below decide.");
        M.put("Wetter nach Genre", "Weather by genre");
        M.put("Rock/Metal/Techno \u2026 = Gewitter, Indie/Jazz/Akustik \u2026 = Regen, Pop/Hip-Hop/Dance \u2026 = klar. Nur bei dir sichtbar.", "Rock/metal/techno \u2026 = thunderstorm, indie/jazz/acoustic \u2026 = rain, pop/hip-hop/dance \u2026 = clear. Only visible to you.");
        M.put("Uhrzeit nach Genre", "Time by genre");
        M.put("Metal/Techno \u2026 = Nacht, R&B/Rock/Hip-Hop \u2026 = Sonnenuntergang, Klassik/Folk \u2026 = Sonnenaufgang, Pop/Dance \u2026 = Mittag. Die Zeit l\u00e4uft im Zeitraffer hin. Nur bei dir sichtbar.", "Metal/techno \u2026 = night, R&B/rock/hip-hop \u2026 = sunset, classical/folk \u2026 = sunrise, pop/dance \u2026 = noon. Time moves there in a time-lapse. Only visible to you.");
        M.put("PvP-Edit", "PvP Edit");
        M.put("auch Elytra-/Mace-Kampf", "incl. Elytra/Mace combat");
        M.put("Wenn du gegen einen anderen Spieler k\u00e4mpfst, steht der Songtext Wort f\u00fcr Wort neben ihm \u2013 wie in einem TikTok-Edit. Mobs z\u00e4hlen nicht.", "When you fight another player, the lyrics appear next to them word by word \u2013 like a music edit. Mobs don't count.");
        M.put("PvP-Textgr\u00f6\u00dfe", "PvP text size");
        M.put("Nach der eingestellten Zeit ohne Eingabe zeigt die Kamera Kino-Shots von deinem Skin. Jede Eingabe beendet es sofort.", "After the set time without input, the camera shows cinematic shots of your skin. Any input ends it immediately.");
        M.put("AFK nach", "AFK after");
        M.put("Shot-L\u00e4nge", "Shot length");
        M.put("schwarze Kino-Balken", "black cinema bars");
        M.put("AFK-Timer", "AFK timer");
        M.put("Landschafts-Shots", "Landscape shots");
        M.put("Landschaft, Drohne, Himmel", "landscape, drone, sky");
        M.put("Mob-Shots", "Mob shots");
        M.put("schaut Mobs in der N\u00e4he an", "looks at nearby mobs");
        M.put("\u00dcber die Schulter auf einen Mob in der N\u00e4he, oder aus Sicht des Mobs auf dich.", "Over the shoulder at a nearby mob, or from the mob's view towards you.");
        M.put("Auch im Chat/Pausemen\u00fc", "Also in chat/pause menu");
        M.put("HUD ausblenden", "Hide HUD");
        M.put("Schrift", "Font");
        M.put("Beispiel Aa", "Sample Aa");
        M.put("Gerade l\u00e4uft nichts", "Nothing is playing");
        M.put("Starte Musik in Spotify (oder Browser).", "Start music in Spotify (or a browser).");
        M.put("Song-Erkennung gibt es nur unter Windows.", "Song detection only works on Windows.");
        M.put("\u25b6 L\u00c4UFT GERADE", "\u25b6 NOW PLAYING");
        M.put("\u275a\u275a PAUSIERT", "\u275a\u275a PAUSED");
        M.put("\u2248 Lyrics ungef\u00e4hr (nicht getaktet)", "\u2248 Approximate lyrics (not synced)");
        M.put("Kein Song erkannt", "No song detected");
        M.put("Lyrics-Sync", "Lyrics sync");
        M.put("Spulen", "Seek");
        M.put("Klicken oder ziehen zum Spulen", "Click or drag to seek");
        M.put("Lautst\u00e4rke", "Volume");
        M.put("Spotify-Lautst\u00e4rke (klicken, ziehen oder Mausrad)", "Spotify volume (click, drag or scroll)");
        M.put("\u21c4 sucht passende Version \u2026", "\u21c4 looking for matching version \u2026");
        M.put("\u21c4 gleicht ab \u2026 (", "\u21c4 syncing \u2026 (");
        M.put("\u21c4 Sync korrigiert (", "\u21c4 sync corrected (");
        M.put("\u2713 synchron \u00b7 Tempo angeglichen (\u00d7%.2f)", "\u2713 in sync \u00b7 tempo matched (\u00d7%.2f)");
        M.put("\u2713 synchron \u00b7 passende Version geladen", "\u2713 in sync \u00b7 matching version loaded");
        M.put("\u2713 synchron \u00b7 Song-Versatz ", "\u2713 in sync \u00b7 song offset ");
        M.put("\u2248 andere Version (", "\u2248 different version (");
        M.put(") \u00b7 \u2212/+ merkt's", ") \u00b7 \u2212/+ remembers it");
        M.put("\u2713 Lyrics getaktet \u00b7 synchron", "\u2713 Synced lyrics \u00b7 in sync");
        M.put("aus", "off");
        M.put("nur unter Windows", "Windows only");
        M.put("wartet auf Musik", "waiting for music");
        M.put("h\u00f6rt zu \u2026", "listening \u2026");
        M.put("Sonnenaufgang", "Sunrise");
        M.put("Mittag", "Noon");
        M.put("Sonnenuntergang", "Sunset");
        M.put("Nacht", "Night");
        M.put("kein Song", "no song");
        M.put("Genre wird gesucht\u2026", "looking up genre\u2026");
        M.put("Genre unbekannt", "genre unknown");
        M.put("klar", "clear");
        M.put("Regen", "rain");
        M.put("Gewitter", "thunderstorm");
        M.put("l\u00e4dt \u2026", "loading \u2026");
        M.put("keine Lyrics", "no lyrics");
        M.put("l\u00e4dt \u2026 (Versuch ", "loading \u2026 (attempt ");
        M.put("neuer Versuch in ", "retrying in ");
        M.put("\u2248 ungef\u00e4hr", "\u2248 approx.");
        M.put("Lyrics im Himmel ", "Sky lyrics ");
        M.put("\u00a7aan", "\u00a7aon");
        M.put("\u00a7caus", "\u00a7coff");
        M.put("Lyrics-Versatz", "Lyrics offset");
        M.put(" (dieser Song)", " (this song)");
        M.put("\u00a76[AFK Cinema] \u00a7aan", "\u00a76[AFK Cinema] \u00a7aon");
        M.put("\u00a76[AFK Cinema] \u00a7caus", "\u00a76[AFK Cinema] \u00a7coff");
        M.put("Glatt", "Smooth");
    }
}

