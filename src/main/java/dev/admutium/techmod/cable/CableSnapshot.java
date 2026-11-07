package dev.admutium.techmod.cable;

import java.util.List;

/** Immutable render-thread snapshot; never expose mutable block entity collections. */
public record CableSnapshot(long wires, int holders, List<CableSpan> spans) {
    public static final CableSnapshot EMPTY = new CableSnapshot(0, 0, List.of());
    public CableSnapshot { spans = List.copyOf(spans); }
    public boolean has(Node n) { return (wires & n.bit()) != 0; }
    public boolean holder(Face f) { return (holders & (1 << f.ordinal())) != 0; }
    public boolean empty() { return wires == 0 && holders == 0 && spans.isEmpty(); }
}
