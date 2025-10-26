package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.repository.user.BannerRepository;
import com.cakestore.cakestore.repository.user.CategoryRepository;
import com.cakestore.cakestore.repository.user.ProductRepository;
import com.cakestore.cakestore.service.user.ProductQueryService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class ShopController {

    private final ProductQueryService productSvc;
    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;
    private final BannerRepository bannerRepo; // <--- thêm

    public ShopController(ProductQueryService svc,
            CategoryRepository c,
            ProductRepository p,
            BannerRepository b) { // <--- thêm
        this.productSvc = svc;
        this.categoryRepo = c;
        this.productRepo = p;
        this.bannerRepo = b; // <--- thêm
    }

    @GetMapping({ "/", "/home" })
    public String home(Model m, HttpServletRequest request) {
        m.addAttribute("topSelling", productSvc.homeTopSelling());
        m.addAttribute("banners", bannerRepo.findByIsActiveTrueOrderBySortOrderAscIdAsc()); // <--- thêm
        m.addAttribute("categories", categoryRepo.findByIsActiveTrueOrderBySortOrderAscIdAsc());

        // ===== Recently Viewed từ cookie "RV" =====
        List<Long> ids = parseRecentlyIds(request, 12); // tối đa 12 id
        var byId = productRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(p -> p.getId(), p -> p));
        var recently = ids.stream().map(byId::get).filter(Objects::nonNull).toList();
        m.addAttribute("recently", recently);

        return "user/home";
    }

    @GetMapping("/product/{id}")
    public String detail(@PathVariable Long id, Model m) {
        m.addAttribute("p", productSvc.detail(id));
        return "user/product-detail";
    }

    @GetMapping("/category/{slug}")
    public String byCategory(@PathVariable String slug,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model m) {
        var cat = categoryRepo.findBySlug(slug).orElseThrow();
        var data = productSvc.search(cat.getId(), q, page, 20, sort);
        m.addAttribute("category", cat);
        m.addAttribute("data", data);
        m.addAttribute("q", q);
        m.addAttribute("sort", sort);
        return "user/product-category";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model m) {
        var data = productSvc.search(null, q, page, 20, sort);
        m.addAttribute("data", data);
        m.addAttribute("q", q);
        m.addAttribute("sort", sort);
        return "user/product-search";
    }

    // ===== helpers =====
    private List<Long> parseRecentlyIds(HttpServletRequest req, int limit) {
        if (req.getCookies() == null)
            return List.of();
        String raw = Stream.of(req.getCookies())
                .filter(c -> "RV".equals(c.getName()))
                .findFirst().map(Cookie::getValue).orElse(null);
        if (raw == null || raw.isBlank())
            return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim).filter(s -> !s.isBlank())
                .map(s -> {
                    try {
                        return Long.parseLong(s);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct() // chống trùng
                .limit(limit)
                .toList();
    }
}
