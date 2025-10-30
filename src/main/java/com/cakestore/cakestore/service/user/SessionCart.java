// ...existing code...
package com.cakestore.cakestore.service.user;

import java.math.BigDecimal;
import java.util.*;

/*
...existing code...
*/
public class SessionCart {

    public static class Line {
        public Long productId;
        public Long variantId; // có thể null
        public String name;
        public BigDecimal price = BigDecimal.ZERO;
        public int qty = 0;

        public Long getProductId() {
            return productId;
        }

        public Long getVariantId() {
            return variantId;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public int getQty() {
            return qty;
        }

        // Template expects l.unitPrice and l.lineTotal()
        // Provide compatibility getters:

        public BigDecimal getUnitPrice() {
            return price;
        }

        public BigDecimal getLineTotal() {
            return price.multiply(BigDecimal.valueOf(qty));
        }

        public BigDecimal lineTotal() {
            return getLineTotal();
        }
    }

    private final Map<String, Line> lines = new LinkedHashMap<>();

    private String key(Long pId, Long vId) {
        return pId + ":" + (vId == null ? "0" : String.valueOf(vId));
    }

    public Collection<Line> items() {
        return Collections.unmodifiableCollection(lines.values());
    }

    public void add(Long pId, Long vId, String name, BigDecimal price, int qty) {
        if (pId == null || qty <= 0 || price == null)
            return;
        String k = key(pId, vId);
        var l = lines.get(k);
        if (l == null) {
            l = new Line();
            l.productId = pId;
            l.variantId = vId;
            l.name = name;
            l.price = price;
            l.qty = qty;
            lines.put(k, l);
        } else {
            l.qty = Math.max(0, l.qty + qty);
        }
        if (l.qty <= 0)
            lines.remove(k);
    }

    public void setQty(Long pId, Long vId, int qty) {
        if (pId == null)
            return;
        String k = key(pId, vId);
        if (qty <= 0) {
            lines.remove(k);
            return;
        }
        var l = lines.get(k);
        if (l != null) {
            l.qty = qty;
        }
    }

    public void remove(Long pId, Long vId) {
        if (pId == null)
            return;
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
// ...existing code...