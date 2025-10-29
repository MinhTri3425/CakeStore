package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.dto.user.ProductCardDto;
import com.cakestore.cakestore.repository.user.BannerRepository;
import com.cakestore.cakestore.repository.user.BranchInventoryRepository;
import com.cakestore.cakestore.repository.user.CategoryRepository;
import com.cakestore.cakestore.repository.user.ProductRepository;
import com.cakestore.cakestore.service.user.BranchContextService;
import com.cakestore.cakestore.service.user.ProductQueryService;
import com.cakestore.cakestore.service.user.RecentlyViewedService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ShopController {

        private final ProductQueryService productSvc;
        private final CategoryRepository categoryRepo;
        private final ProductRepository productRepo;
        private final BannerRepository bannerRepo;

        private final BranchContextService branchCtx;
        private final BranchInventoryRepository branchInvRepo;

        private final RecentlyViewedService recentlyViewedService;

        public ShopController(
                        ProductQueryService svc,
                        CategoryRepository c,
                        ProductRepository p,
                        BannerRepository b,
                        BranchContextService branchCtx,
                        BranchInventoryRepository branchInvRepo,
                        RecentlyViewedService recentlyViewedService) {
                this.productSvc = svc;
                this.categoryRepo = c;
                this.productRepo = p;
                this.bannerRepo = b;
                this.branchCtx = branchCtx;
                this.branchInvRepo = branchInvRepo;
                this.recentlyViewedService = recentlyViewedService;
        }

        // ================= HOME PUBLIC ("/" và "/home") =================
        @GetMapping({ "/", "/home" })
        public String home(
                        Model m,
                        HttpServletRequest request,
                        HttpSession session,
                        Authentication auth) {

                // 1. Xác định chi nhánh hiện tại
                var currentBranch = branchCtx
                                .resolveCurrentBranch(session, request)
                                .orElse(null);
                Long branchId = (currentBranch != null ? currentBranch.getId() : null);

                // 2. Top bán chạy theo chi nhánh
                m.addAttribute(
                                "topSelling",
                                productSvc.homeTopSellingInBranch(branchId, false) // false = 12 sản phẩm
                );

                // 3. Banner + danh mục
                m.addAttribute(
                                "banners",
                                bannerRepo.findByIsActiveTrueOrderBySortOrderAscIdAsc());
                m.addAttribute(
                                "categories",
                                categoryRepo.findByIsActiveTrueOrderBySortOrderAscIdAsc());

                // 4. Recently viewed (lấy từ DB theo user đăng nhập)
                List<ProductCardDto> recentlyList = List.of();
                if (auth != null && auth.isAuthenticated()) {
                        recentlyList = recentlyViewedService.getRecentlyViewedDtos(auth.getName(), 12);
                }
                m.addAttribute("recently", recentlyList);

                System.out.println("[home] user=" +
                                (auth != null ? auth.getName() : "guest") +
                                " recently size=" + recentlyList.size());

                // 5. Thông tin branch cho header
                m.addAttribute("currentBranch", currentBranch);

                return "user/home";
        }

        // ================= HOME USER (/user/home) =================
        @GetMapping("/user/home")
        public String homeuser(
                        Model m,
                        HttpServletRequest request,
                        HttpSession session,
                        Authentication auth) {

                var currentBranch = branchCtx
                                .resolveCurrentBranch(session, request)
                                .orElse(null);
                Long branchId = (currentBranch != null ? currentBranch.getId() : null);

                // show nhiều hơn -> true = 24 sản phẩm thay vì 12
                m.addAttribute(
                                "topSelling",
                                productSvc.homeTopSellingInBranch(branchId, true));

                m.addAttribute(
                                "banners",
                                bannerRepo.findByIsActiveTrueOrderBySortOrderAscIdAsc());
                m.addAttribute(
                                "categories",
                                categoryRepo.findByIsActiveTrueOrderBySortOrderAscIdAsc());

                List<ProductCardDto> recentlyList = List.of();
                if (auth != null && auth.isAuthenticated()) {
                        recentlyList = recentlyViewedService.getRecentlyViewedDtos(auth.getName(), 12);
                }
                m.addAttribute("recently", recentlyList);

                m.addAttribute("currentBranch", currentBranch);

                System.out.println("[homeuser] user=" +
                                (auth != null ? auth.getName() : "guest") +
                                " recently size=" + recentlyList.size());

                return "user/home";
        }

        // ================= PRODUCT DETAIL =================
        @GetMapping("/product/{id}")
        public String detail(
                        @PathVariable Long id,
                        Model m,
                        HttpSession session,
                        HttpServletRequest request,
                        Authentication auth) {

                // 1. Lấy DTO chi tiết sản phẩm
                var dto = productSvc.detail(id);
                if (dto == null) {
                        return "redirect:/";
                }
                m.addAttribute("p", dto);

                // 2. Chi nhánh hiện tại
                var currentBranch = branchCtx
                                .resolveCurrentBranch(session, request)
                                .orElse(null);
                m.addAttribute("currentBranch", currentBranch);

                // 3. Tồn kho theo branch
                Integer branchStockAvailable = null;
                if (currentBranch != null) {
                        branchStockAvailable = branchInvRepo
                                        .findByBranch_IdAndProduct_Id(currentBranch.getId(), id)
                                        .map(inv -> inv.getAvailable())
                                        .orElse(0);
                }
                m.addAttribute("branchStockAvailable", branchStockAvailable);

                // 4. Ghi nhận lịch sử xem gần đây cho user đăng nhập
                if (auth != null && auth.isAuthenticated()) {
                        recentlyViewedService.recordView(auth.getName(), id);
                }

                return "user/product-detail";
        }

        // ================= CATEGORY PAGE =================
        @GetMapping("/category/{slug}")
        public String byCategory(
                        @PathVariable String slug,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false, defaultValue = "newest") String sort,
                        @RequestParam(defaultValue = "0") int page,
                        Model m,
                        HttpSession session,
                        HttpServletRequest request,
                        Authentication auth) {

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

                List<ProductCardDto> recentlyList = List.of();
                if (auth != null && auth.isAuthenticated()) {
                        recentlyList = recentlyViewedService.getRecentlyViewedDtos(auth.getName(), 12);
                }
                m.addAttribute("recently", recentlyList);

                return "user/product-category";
        }

        // ================= SEARCH PAGE =================
        @GetMapping("/search")
        public String search(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false, defaultValue = "newest") String sort,
                        @RequestParam(defaultValue = "0") int page,
                        Model m,
                        HttpSession session,
                        HttpServletRequest request,
                        Authentication auth) {

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

                List<ProductCardDto> recentlyList = List.of();
                if (auth != null && auth.isAuthenticated()) {
                        recentlyList = recentlyViewedService.getRecentlyViewedDtos(auth.getName(), 12);
                }
                m.addAttribute("recently", recentlyList);

                return "user/product-search";
        }
}
