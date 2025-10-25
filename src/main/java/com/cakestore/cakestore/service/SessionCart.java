package com.cakestore.cakestore.service;

import java.math.BigDecimal;
import java.util.*;

public class SessionCart {

    public static class Line {
        public Long productId;
        public Long variantId; // có thể null
        public String name;
        public BigDecimal unitPrice;
        public int qty;

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(qty));
        }
    }

    private final Map<String, Line> lines = new LinkedHashMap<>();

    private String key(Long pId, Long vId) {
        return pId + ":" + (vId == null ? "-" : vId);
    }

    public Collection<Line> items() {
        return lines.values();
    }

    public void add(Long pId, Long vId, String name, BigDecimal price, int qty) {
        String k = key(pId, vId);
        Line l = lines.get(k);
        if (l == null) {
            l = new Line();
            l.productId = pId;
            l.variantId = vId;
            l.name = name;
            l.unitPrice = price;
            l.qty = 0;
            lines.put(k, l);
        }
        l.qty += Math.max(1, qty);
    }

    public void setQty(Long pId, Long vId, int qty) {
        String k = key(pId, vId);
        var l = lines.get(k);
        if (l != null) {
            if (qty <= 0)
                lines.remove(k);
            else
                l.qty = qty;
        }
    }

    public void remove(Long pId, Long vId) {
        lines.remove(key(pId, vId));
    }

    public BigDecimal subtotal() {
        return lines.values().stream()
                .map(Line::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public void clear() {
        lines.clear();
    }
}
