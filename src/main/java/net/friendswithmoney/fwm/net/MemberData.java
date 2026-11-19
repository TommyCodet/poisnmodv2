/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.net;

import java.util.ArrayList;
import java.util.List;

public class MemberData {
    public List<Member> members = new ArrayList<Member>();
    public List<String> clans = new ArrayList<String>();

    public static class Member {
        public String name;
        public String role;
        public String clan;
        public long joinedAt;
    }
}

