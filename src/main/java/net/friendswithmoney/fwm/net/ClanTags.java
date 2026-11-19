/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.net;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.net.FakeList;
import net.friendswithmoney.fwm.net.Members;
import net.friendswithmoney.fwm.net.TrustedClans;

public final class ClanTags {
    private static final Pattern REAL_TAG = Pattern.compile("(?i)^FWM(?:[0-9]|X)?$");
    private static final Pattern CANDIDATE = Pattern.compile("\\[[^\\[\\]]{1,8}\\]");

    private ClanTags() {
    }

    public static String fakeRawIn(String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }
        Matcher matcher = CANDIDATE.matcher(line);
        while (matcher.find()) {
            String tag = ClanTags.bare(matcher.group());
            if (!ClanTags.isFake(tag)) continue;
            return tag;
        }
        return null;
    }

    public static String fakeIn(String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }
        Matcher matcher = CANDIDATE.matcher(line);
        while (matcher.find()) {
            String tag = ClanTags.bare(matcher.group());
            if (!ClanTags.isFake(tag)) continue;
            return ClanTags.tidy(tag);
        }
        return null;
    }

    public static List<int[]> fakeSpans(String line) {
        if (line == null || line.isEmpty()) {
            return List.of();
        }
        ArrayList<int[]> spans = null;
        Matcher matcher = CANDIDATE.matcher(line);
        while (matcher.find()) {
            if (!ClanTags.isFake(ClanTags.bare(matcher.group()))) continue;
            if (spans == null) {
                spans = new ArrayList<int[]>(2);
            }
            spans.add(new int[]{matcher.start(), matcher.end()});
        }
        return spans == null ? List.of() : spans;
    }

    public static String bare(String token) {
        if (token != null && token.length() > 1 && token.charAt(0) == '[') {
            return token.substring(1, token.length() - 1);
        }
        return token;
    }

    public static boolean isFake(String tag) {
        List<String> exact;
        List<String> fakes;
        if (FakeList.isFakeTag(tag)) {
            return true;
        }
        if (TrustedClans.fakeOf(tag) != null) {
            return true;
        }
        String letters = ClanTags.letters(tag);
        if (letters.isEmpty()) {
            return false;
        }
        if (ClanTags.isReal(tag)) {
            return false;
        }
        if (ClanTags.sameTag(letters, ClanTags.letters("FWM"))) {
            return true;
        }
        boolean managed = FakeList.managed();
        List<String> list = fakes = managed ? ClanTags.ownOnly(FwmConfig.INSTANCE.fakeTags, FwmConfig.DEFAULT_FAKE_TAGS) : FwmConfig.INSTANCE.fakeTags;
        if (managed) {
            for (String fake : FakeList.similarTags()) {
                if (!ClanTags.sameTag(letters, ClanTags.letters(fake))) continue;
                return true;
            }
        }
        if (fakes != null) {
            for (String fake : fakes) {
                if (!ClanTags.sameTag(letters, ClanTags.letters(fake))) continue;
                return true;
            }
        }
        List<String> list2 = exact = managed ? ClanTags.ownOnly(FwmConfig.INSTANCE.fakeTagsExact, FwmConfig.DEFAULT_FAKE_TAGS_EXACT) : FwmConfig.INSTANCE.fakeTagsExact;
        if (exact != null) {
            String plain = ClanTags.tidy(tag);
            for (String fake : exact) {
                if (fake == null || !plain.equals(ClanTags.tidy(fake))) continue;
                return true;
            }
        }
        return false;
    }

    private static List<String> ownOnly(List<String> mine, List<String> builtIn) {
        if (mine == null || mine.isEmpty()) {
            return List.of();
        }
        HashSet<String> skip = new HashSet<String>();
        for (String s : builtIn) {
            skip.add(ClanTags.tidy(s));
        }
        ArrayList<String> out = new ArrayList<String>();
        for (String s : mine) {
            if (s == null || skip.contains(ClanTags.tidy(s))) continue;
            out.add(s);
        }
        return out;
    }

    private static boolean sameTag(String letters, String fake) {
        if (letters.equals(fake)) {
            return true;
        }
        String bare = fake.replace("X", "");
        return bare.length() >= 3 && letters.replace("X", "").equals(bare);
    }

    public static boolean isReal(String tag) {
        if (tag == null) {
            return false;
        }
        String plain = tag.trim().replaceAll("[^\\x00-\\x7F]", "");
        if (TrustedClans.isFwmTag(plain)) {
            return true;
        }
        if (Members.clanTags().contains(plain.toUpperCase(Locale.ROOT))) {
            return true;
        }
        return REAL_TAG.matcher(plain).matches();
    }

    private static String letters(String tag) {
        if (tag == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(tag.length());
        for (int i = 0; i < tag.length(); ++i) {
            char c = Character.toUpperCase(tag.charAt(i));
            if (c == 'O' || c == 'I' || c < 'A' || c > 'Z') continue;
            out.append(c);
        }
        return out.toString();
    }

    public static String tidy(String tag) {
        return tag == null ? "" : tag.trim().toUpperCase(Locale.ROOT);
    }
}

