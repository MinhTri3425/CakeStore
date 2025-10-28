package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.repository.user.BannerRepository;
import com.cakestore.cakestore.repository.user.BranchInventoryRepository;
import com.cakestore.cakestore.repository.user.CategoryRepository;
import com.cakestore.cakestore.repository.user.ProductRepository;
import com.cakestore.cakestore.service.user.BranchContextService;
import com.cakestore.cakestore.service.user.ProductQueryService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ShopController {

    private final ProductQueryService productSvc;
    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;
    private final BannerRepository bannerRepo;

    private final BranchContextService branchCtx;
    private final BranchInventoryRepository branchInvRepo;

    public ShopController(
            ProductQueryService svc,
            CategoryRepository c,
            ProductRepository p,
            BannerRepository b,
            BranchContextService branchCtx,
            BranchInventoryRepository branchInvRepo) {
        this.productSvc = svc;
        this.categoryRepo = c;
        this.productRepo = p;
        this.bannerRepo = b;
        this.branchCtx = branchCtx;
        this.branchInvRepo = branchInvRepo;
    }

    @GetMapping({ "/", "/home" })
    public String home(Model m, HttpServletRequest request, HttpSession session) {

        // lấy chi nhánh hiện tại
        var currentBranch = branchCtx
                .resolveCurrentBranch(session, request)
                .orElse(null);
        Long branchId = (currentBranch != null ? currentBranch.getId() : null);

        // danh sách bán chạy (lọc theo branch)
        m.addAttribute("topSelling",
                productSvc.homeTopSellingInBranch(branchId, false)); // false = 12 sp

        // banner + categories
        m.addAttribute("banners",
                bannerRepo.findByIsActiveTrueOrderBySortOrderAscIdAsc());
        m.addAttribute("categories",
                categoryRepo.findByIsActiveTrueOrderBySortOrderAscIdAsc());

        // recently viewed
        m.addAttribute("recently", buildRecentlyList(request));

        // để header / view biết chi nhánh nào đang active
        m.addAttribute("currentBranch", currentBranch);

        return "user/home";
    }

    @GetMapping({ "/user/home" })
    public String homeuser(Model m, HttpServletRequest request, HttpSession session) {

        var currentBranch = branchCtx
                .resolveCurrentBranch(session, request)
                .orElse(null);
        Long branchId = (currentBranch != null ? currentBranch.getId() : null);

        m.addAttribute("topSelling",
                productSvc.homeTopSellingInBranch(branchId, true)); // true = 24 sp

        m.addAttribute("banners",
                bannerRepo.findByIsActiveTrueOrderBySortOrderAscIdAsc());
        m.addAttribute("categories",
                categoryRepo.findByIsActiveTrueOrderBySortOrderAscIdAsc());

        m.addAttribute("recently", buildRecentlyList(request));
        m.addAttribute("currentBranch", currentBranch);

        return "user/home";
    }

    @GetMapping("/product/{id}")
    public String detail(
            @PathVariable Long id,
            Model m,
            HttpSession session,
            HttpServletRequest request) {

        // 1. lấy dto chi tiết
        var dto = productSvc.detail(id);
        if (dto == null) {
            return "redirect:/";
        }
        m.addAttribute("p", dto);

        // 2. lấy chi nhánh hiện tại
        var currentBranch = branchCtx
                .resolveCurrentBranch(session, request)
                .orElse(null);
        m.addAttribute("currentBranch", currentBranch);

        // 3. lookup tồn kho theo branch
        Integer branchStockAvailable = null;
        if (currentBranch != null) {
            branchStockAvailable = branchInvRepo
                    .findByBranch_IdAndProduct_Id(currentBranch.getId(), id)
                    .map(inv -> inv.getAvailable())
                    .orElse(0);
        }
        m.addAttribute("branchStockAvailable", branchStockAvailable);

        return "user/product-detail";
    }

    @GetMapping("/category/{slug}")
    public String byCategory(
            @PathVariable String slug,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model m,
            HttpSession session,
            HttpServletRequest request) {

        var cat = categoryRepo.findBySlug(slug).orElseThrow();

        var currentBranch = branchCtx
                .resolveCurrentBranch(session, request)
                .orElse(null);
        Long branchId = (currentBranch != null ? currentBranch.getId() : null);

        var data = productSvc.searchInBranch(
                branchId,
                cat.getId(),
                q,
                page,
                20,
                sort);

        m.addAttribute("category", cat);
        m.addAttribute("data", data);
        m.addAttribute("q", q);
        m.addAttribute("sort", sort);
        m.addAttribute("currentBranch", currentBranch);

        return "user/product-category";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model m,
            HttpSession session,
            HttpServletRequest request) {

        var currentBranch = branchCtx
                .resolveCurrentBranch(session, request)
                .orElse(null);
        Long branchId = (currentBranch != null ? currentBranch.getId() : null);

        var data = productSvc.searchInBranch(
                branchId,
                null,
                q,
                page,
                20,
                sort);

        m.addAttribute("data", data);
        m.addAttribute("q", q);
        m.addAttribute("sort", sort);
        m.addAttribute("currentBranch", currentBranch);

        return "user/product-search";
    }

    // ===== helpers =====
    private List<com.cakestore.cakestore.entity.Product> buildRecentlyList(HttpServletRequest request) {
        List<Long> ids = parseRecentlyIds(request, 12); // tối đa 12 id
        var byId = productRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(p -> p.getId(), p -> p));
        return ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<Long> parseRecentlyIds(HttpServletRequest req, int limit) {
        if (req.getCookies() == null)
            return List.of();

        String raw = Arrays.stream(req.getCookies())
                .filter(c -> "RV".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (raw == null || raw.isBlank())
            return List.of();

        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> {
                    try {
                        return Long.parseLong(s);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .limit(limit)
                .toList();
    }
}
