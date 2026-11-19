/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  net.fabricmc.loader.api.FabricLoader
 */
package net.friendswithmoney.fwm.auction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.auction.Auction;
import net.friendswithmoney.fwm.util.Fmt;

public final class AuctionStats {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX = 2000;
    private static List<Record> records;
    private static long lastRecorded;

    private AuctionStats() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("fwm").resolve("auctions.json");
    }

    public static List<Record> all() {
        if (records == null) {
            AuctionStats.load();
        }
        return records;
    }

    private static void load() {
        records = new ArrayList<Record>();
        try {
            List read;
            if (Files.exists(AuctionStats.file(), new LinkOption[0]) && (read = (List)GSON.fromJson(Files.readString(AuctionStats.file(), StandardCharsets.UTF_8), new TypeToken<List<Record>>(){}.getType())) != null) {
                records.addAll(read);
            }
        }
        catch (Exception e) {
            System.err.println("[FWM] Could not read auctions.json: " + e.getMessage());
        }
    }

    private static void save() {
        try {
            Path f = AuctionStats.file();
            Files.createDirectories(f.getParent(), new FileAttribute[0]);
            Path tmp = f.resolveSibling("auctions.json.tmp");
            Files.writeString(tmp, (CharSequence)GSON.toJson(records), StandardCharsets.UTF_8, new OpenOption[0]);
            Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e) {
            System.err.println("[FWM] Could not write auctions.json: " + e.getMessage());
        }
    }

    static void record(long endedAt, String item, String winner, long price, long earnings, List<Auction.Bid> bids, List<Auction.Bid> late) {
        if (endedAt <= 0L || endedAt == lastRecorded) {
            return;
        }
        if ((bids == null || bids.isEmpty()) && (late == null || late.isEmpty())) {
            return;
        }
        lastRecorded = endedAt;
        Record r = new Record();
        r.at = endedAt;
        r.item = item;
        r.winner = winner;
        r.price = price;
        r.earnings = earnings;
        if (bids != null) {
            for (Auction.Bid b : bids) {
                r.paid.add(AuctionStats.paid(b, false));
            }
        }
        if (late != null) {
            for (Auction.Bid b : late) {
                r.paid.add(AuctionStats.paid(b, true));
            }
        }
        AuctionStats.all().add(r);
        while (records.size() > 2000) {
            records.remove(0);
        }
        AuctionStats.save();
    }

    private static Paid paid(Auction.Bid b, boolean late) {
        Paid p = new Paid();
        p.name = b.name();
        p.amount = b.amount();
        p.late = late;
        return p;
    }

    public static void reset() {
        records = new ArrayList<Record>();
        AuctionStats.save();
    }

    private static boolean inRange(Record r, Range range) {
        if (range == Range.ALL) {
            return true;
        }
        LocalDate day = Instant.ofEpochMilli(r.at).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();
        return switch (range.ordinal()) {
            case 0 -> day.equals(today);
            case 1 -> {
                if (!day.isBefore(today.minusDays(6L))) {
                    yield true;
                }
                yield false;
            }
            case 2 -> {
                if (!day.isBefore(today.minusDays(29L))) {
                    yield true;
                }
                yield false;
            }
            default -> true;
        };
    }

    public static List<Row> board(Board board, Range range, int limit) {
        HashMap<String, Long> sum = new HashMap<String, Long>();
        HashMap<String, String> shown = new HashMap<String, String>();
        for (Record r : AuctionStats.all()) {
            if (!AuctionStats.inRange(r, range)) continue;
            HashSet<String> joined = new HashSet<String>();
            for (Paid p : r.paid) {
                String key = p.name.toLowerCase(Locale.ROOT);
                shown.putIfAbsent(key, p.name);
                switch (board.ordinal()) {
                    case 0: {
                        sum.merge(key, p.amount, Long::sum);
                        break;
                    }
                    case 2: {
                        if (!joined.add(key)) break;
                        sum.merge(key, 1L, Long::sum);
                        break;
                    }
                    case 3: {
                        if (!p.late) break;
                        sum.merge(key, p.amount, Math::max);
                        break;
                    }
                }
            }
            if (board != Board.WINS || r.winner == null) continue;
            String key = r.winner.toLowerCase(Locale.ROOT);
            shown.putIfAbsent(key, r.winner);
            sum.merge(key, 1L, Long::sum);
        }
        ArrayList<Row> rows = new ArrayList<Row>();
        for (Map.Entry e : sum.entrySet()) {
            rows.add(new Row((String)shown.get(e.getKey()), (Long)e.getValue()));
        }
        rows.sort(Comparator.comparingLong(Row::value).reversed());
        return rows.size() > limit ? rows.subList(0, limit) : rows;
    }

    public static long[] own(Range range) {
        long count = 0L;
        long earnings = 0L;
        long late = 0L;
        for (Record r : AuctionStats.all()) {
            if (!AuctionStats.inRange(r, range)) continue;
            ++count;
            earnings += r.earnings;
            for (Paid p : r.paid) {
                if (!p.late) continue;
                late += p.amount;
            }
        }
        return new long[]{count, earnings, late, count == 0L ? 0L : earnings / count};
    }

    public static String export(Board board, Range range) {
        StringBuilder b = new StringBuilder("FWM Auktionen \u00b7 " + String.valueOf((Object)board) + " \u00b7 " + String.valueOf((Object)range) + "\n");
        int i = 1;
        for (Row row : AuctionStats.board(board, range, 20)) {
            String value = board == Board.WINS || board == Board.JOINED ? String.valueOf(row.value()) : Fmt.compactMoney(row.value());
            b.append(i++).append(". ").append(row.name()).append(" \u2013 ").append(value).append('\n');
        }
        return b.toString();
    }

    public static final class Record {
        public long at;
        public String item;
        public String winner;
        public long price;
        public long earnings;
        public List<Paid> paid = new ArrayList<Paid>();
    }

    public static final class Paid {
        public String name;
        public long amount;
        public boolean late;
    }

    public static enum Range {
        TODAY,
        WEEK,
        MONTH,
        ALL;

    }

    public static enum Board {
        PAID,
        WINS,
        JOINED,
        FAIL;

    }

    public record Row(String name, long value) {
    }
}

