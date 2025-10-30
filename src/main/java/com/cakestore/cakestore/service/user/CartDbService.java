package com.cakestore.cakestore.service.user;

import com.cakestore.cakestore.entity.*;
import com.cakestore.cakestore.repository.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CartDbService {
    private static final Logger log = LoggerFactory.getLogger(CartDbService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public CartDbService(CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    /**
     * Merge session → DB (cộng dồn toàn bộ). Dùng duy nhất lúc đầu (khi DB cart còn
     * trống).
     */
    @Transactional
    public void mergeCart(Authentication auth, SessionCart sessionCart) {
        if (auth == null || !auth.isAuthenticated() || sessionCart == null || sessionCart.isEmpty())
            return;

        String principal = extractPrincipalName(auth);
        if (principal == null)
            return;

        User user = findUserByCommonMethods(principal);
        if (user == null || user.getId() == null)
            return;

        Cart cart = cartRepository.findByUserId(user.getId())
                .or(() -> cartRepository.findByUser_Id(user.getId()))
                .orElseGet(() -> createCart(user));

        Long cartId = cart.getId();
        if (cartId == null)
            return;

        for (SessionCart.Line line : sessionCart.items()) {
            Long pid = line.getProductId();
            Long vid = line.getVariantId();
            if (pid == null || line.getQty() <= 0)
                continue;

            try {
                // validate variant thuộc product, nếu sai -> bỏ variant
                if (vid != null) {
                    ProductVariant v = productVariantRepository.findById(vid).orElse(null);
                    if (v == null || v.getProduct() == null || !Objects.equals(v.getProduct().getId(), pid)) {
                        vid = null;
                    }
                }

                Optional<CartItem> opt = (vid != null)
                        ? cartItemRepository.findByCart_IdAndProduct_IdAndVariant_Id(cartId, pid, vid)
                        : cartItemRepository.findByCart_IdAndProduct_IdAndVariantIsNull(cartId, pid);

                if (opt.isPresent()) {
                    CartItem it = opt.get();
                    int newQty = (it.getQuantity() == null ? 0 : it.getQuantity()) + line.getQty();
                    it.setQuantity(newQty);

                    if (line.getPrice() != null)
                        it.setUnitPrice(line.getPrice());

                    cartItemRepository.save(it);
                } else {
                    Product product = productRepository.getReferenceById(pid);
                    ProductVariant variant = (vid != null)
                            ? productVariantRepository.findById(vid).orElse(null)
                            : null;

                    BigDecimal unitPrice = line.getPrice() != null
                            ? line.getPrice()
                            : (product != null ? product.getPrice() : BigDecimal.ZERO);

                    CartItem ci = new CartItem();
                    ci.setCart(cart);
                    ci.setProduct(product);
                    if (variant != null)
                        ci.setVariant(variant);
                    ci.setQuantity(line.getQty());
                    ci.setUnitPrice(unitPrice);

                    cartItemRepository.save(ci);
                }
            } catch (DataIntegrityViolationException ex) {
                log.error("Merge cart fail (cartId={}, pid={}, vid={}, qty={}): {}",
                        cartId, pid, vid, line.getQty(), ex.getMessage(), ex);
                throw ex;
            } catch (RuntimeException ex) {
                log.error("Unexpected merge error (cartId={}, pid={}, vid={}): {}",
                        cartId, pid, vid, ex.getMessage(), ex);
                throw ex;
            }
        }
    }

    /**
     * Add đúng một item vào cart DB (cộng dồn riêng cho sản phẩm đó).
     * Không đụng tới các item khác => không còn bug nhân đôi.
     */
    @Transactional
    public void addItem(Authentication auth,
            Long productId,
            Long variantId,
            String name,
            BigDecimal unitPrice,
            int qty) {

        if (auth == null || !auth.isAuthenticated() || productId == null || qty <= 0)
            return;

        String principal = extractPrincipalName(auth);
        User user = findUserByCommonMethods(principal);
        if (user == null || user.getId() == null)
            return;

        Cart cart = cartRepository.findByUserId(user.getId())
                .or(() -> cartRepository.findByUser_Id(user.getId()))
                .orElseGet(() -> createCart(user));

        Long cartId = cart.getId();
        if (cartId == null)
            return;

        // validate variant chính chủ product
        Long safeVid = variantId;
        if (safeVid != null) {
            ProductVariant v = productVariantRepository.findById(safeVid).orElse(null);
            if (v == null || v.getProduct() == null || !Objects.equals(v.getProduct().getId(), productId)) {
                safeVid = null;
            }
        }

        Optional<CartItem> opt = (safeVid != null)
                ? cartItemRepository.findByCart_IdAndProduct_IdAndVariant_Id(cartId, productId, safeVid)
                : cartItemRepository.findByCart_IdAndProduct_IdAndVariantIsNull(cartId, productId);

        if (opt.isPresent()) {
            // có sẵn -> cộng thêm qty
            CartItem it = opt.get();
            int oldQty = (it.getQuantity() == null ? 0 : it.getQuantity());
            it.setQuantity(oldQty + qty);

            if (unitPrice != null) {
                it.setUnitPrice(unitPrice);
            }
            cartItemRepository.save(it);

        } else {
            // chưa có -> tạo dòng mới
            Product product = productRepository.getReferenceById(productId);
            ProductVariant variant = null;
            if (safeVid != null) {
                variant = productVariantRepository.findById(safeVid).orElse(null);
            }

            BigDecimal priceToUse = (unitPrice != null)
                    ? unitPrice
                    : (product != null ? product.getPrice() : BigDecimal.ZERO);

            CartItem ci = new CartItem();
            ci.setCart(cart);
            ci.setProduct(product);
            if (variant != null) {
                ci.setVariant(variant);
            }
            ci.setQuantity(qty);
            ci.setUnitPrice(priceToUse);

            cartItemRepository.save(ci);
        }
    }

    /**
     * Đặt số lượng tuyệt đối trong DB cho một dòng (set-qty).
     * qty <= 0 thì xoá dòng.
     */
    @Transactional
    public void setDbQty(Authentication auth,
            Long productId,
            Long variantId,
            int qty,
            BigDecimal priceForDb) {

        if (auth == null || !auth.isAuthenticated() || productId == null)
            return;

        String principal = extractPrincipalName(auth);
        User user = findUserByCommonMethods(principal);
        if (user == null || user.getId() == null)
            return;

        Cart cart = cartRepository.findByUserId(user.getId())
                .or(() -> cartRepository.findByUser_Id(user.getId()))
                .orElseGet(() -> createCart(user));

        Long cartId = cart.getId();
        if (cartId == null)
            return;

        Optional<CartItem> opt = (variantId != null)
                ? cartItemRepository.findByCart_IdAndProduct_IdAndVariant_Id(cartId, productId, variantId)
                : cartItemRepository.findByCart_IdAndProduct_IdAndVariantIsNull(cartId, productId);

        if (qty <= 0) {
            // xoá luôn dòng
            opt.ifPresent(cartItemRepository::delete);
            return;
        }

        CartItem it = opt.orElseGet(() -> {
            CartItem ci = new CartItem();
            ci.setCart(cart);
            ci.setProduct(productRepository.getReferenceById(productId));
            if (variantId != null) {
                productVariantRepository.findById(variantId).ifPresent(ci::setVariant);
            }
            return ci;
        });

        it.setQuantity(qty); // đặt tuyệt đối
        if (priceForDb != null)
            it.setUnitPrice(priceForDb);

        cartItemRepository.save(it);
    }

    @Transactional(readOnly = true)
    public SessionCart loadCartAsSessionCart(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return null;

        String principal = extractPrincipalName(auth);
        if (principal == null)
            return null;

        User user = findUserByCommonMethods(principal);
        if (user == null || user.getId() == null)
            return null;

        var dbCartOpt = cartRepository.findByUserId(user.getId())
                .or(() -> cartRepository.findByUser_Id(user.getId()));
        if (dbCartOpt.isEmpty())
            return new SessionCart();

        Cart cart = dbCartOpt.get();
        Long cartId = cart.getId();
        if (cartId == null)
            return new SessionCart();

        var items = Optional.ofNullable(cartItemRepository.findByCart_Id(cartId))
                .orElseGet(() -> cartItemRepository.findByCartId(cartId));

        SessionCart sc = new SessionCart();
        if (items != null) {
            for (CartItem it : items) {
                Long pid = it.getProduct() != null ? it.getProduct().getId() : null;
                Long vid = it.getVariant() != null ? it.getVariant().getId() : null;
                String name = it.getProduct() != null ? it.getProduct().getName() : "Sản phẩm";
                BigDecimal price = it.getUnitPrice();
                int q = it.getQuantity() == null ? 0 : it.getQuantity();

                if (pid != null && q > 0 && price != null) {
                    sc.add(pid, vid, name, price, q);
                }
            }
        }
        return sc;
    }

    @Transactional
    public void removeDbItem(Authentication auth, Long productId, Long variantId) {
        if (auth == null || !auth.isAuthenticated() || productId == null)
            return;

        String principal = extractPrincipalName(auth);
        User user = findUserByCommonMethods(principal);
        if (user == null || user.getId() == null)
            return;

        Cart cart = cartRepository.findByUserId(user.getId())
                .or(() -> cartRepository.findByUser_Id(user.getId()))
                .orElse(null);
        if (cart == null || cart.getId() == null)
            return;

        Long cartId = cart.getId();
        if (variantId != null) {
            cartItemRepository.deleteByCart_IdAndProduct_IdAndVariant_Id(cartId, productId, variantId);
        } else {
            cartItemRepository.deleteByCart_IdAndProduct_IdAndVariantIsNull(cartId, productId);
        }
    }

    @Transactional
    public void clearDbCart(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return;

        String principal = extractPrincipalName(auth);
        User user = findUserByCommonMethods(principal);
        if (user == null || user.getId() == null)
            return;

        cartRepository.findByUserId(user.getId())
                .or(() -> cartRepository.findByUser_Id(user.getId()))
                .ifPresent(c -> cartItemRepository.deleteByCart_Id(c.getId()));
    }

    // ========== internal helpers ==========

    private Cart createCart(User user) {
        Cart c = new Cart();
        c.setUser(user);

        // cố gắng bind chi nhánh (branch) theo cookie / session
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String chosen = null;
                if (req.getSession(false) != null) {
                    Object s = req.getSession(false).getAttribute("BRANCH_ID");
                    if (s != null)
                        chosen = String.valueOf(s);
                }
                if (chosen == null && req.getCookies() != null) {
                    for (Cookie ck : req.getCookies()) {
                        if ("BRANCH_ID".equals(ck.getName())) {
                            chosen = ck.getValue();
                            break;
                        }
                    }
                }
                if (chosen != null && !chosen.isBlank()) {
                    try {
                        branchRepository.findById(Long.parseLong(chosen)).ifPresent(c::setBranch);
                    } catch (Throwable ignore) {
                    }
                }
            }
        } catch (Throwable ignore) {
        }

        if (c.getBranch() == null) {
            branchRepository.findTopByIsActiveTrueOrderByIdAsc()
                    .or(() -> branchRepository.findTopByOrderByIdAsc())
                    .ifPresent(c::setBranch);
        }

        return cartRepository.save(c);
    }

    private String extractPrincipalName(Authentication auth) {
        Object p = auth.getPrincipal();
        if (p instanceof UserDetails ud)
            return ud.getUsername();
        if (p instanceof String s)
            return s;
        return null;
    }

    private User findUserByCommonMethods(String principal) {
        try {
            var m = userRepository.getClass().getMethod("findByEmail", String.class);
            Object r = m.invoke(userRepository, principal);
            User v = unwrapOptionalOrSingle(r, User.class);
            if (v != null)
                return v;
            if (r instanceof User u)
                return u;
        } catch (Throwable ignore) {
        }

        try {
            var m = userRepository.getClass().getMethod("findByUsername", String.class);
            Object r = m.invoke(userRepository, principal);
            User v = unwrapOptionalOrSingle(r, User.class);
            if (v != null)
                return v;
            if (r instanceof User u)
                return u;
        } catch (Throwable ignore) {
        }

        try {
            return userRepository.findById(Long.parseLong(principal)).orElse(null);
        } catch (Throwable ignore) {
        }

        return null;
    }

    private static <T> T unwrapOptionalOrSingle(Object res, Class<T> type) {
        if (res == null)
            return null;
        if (res instanceof Optional<?> opt)
            return type.isInstance(opt.orElse(null)) ? type.cast(opt.orElse(null)) : null;
        if (type.isInstance(res))
            return type.cast(res);
        if (res instanceof Iterable<?> it)
            for (Object o : it)
                if (type.isInstance(o))
                    return type.cast(o);
        return null;
    }
}
